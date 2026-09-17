package project.bookingservice.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import project.bookingservice.client.HotelClient;
import project.bookingservice.entity.Booking;
import project.bookingservice.entity.BookingDetail;
import project.bookingservice.entity.BookingInventory;
import project.bookingservice.mapper.BookingMapper;
import project.bookingservice.repo.BookingDetailRepo;
import project.bookingservice.repo.BookingInventoryRepo;
import project.bookingservice.repo.BookingRepo;
import project.bookingservice.service.impl.OutboxService;
import project.bookingservice.spec.BookingDetailSpec;
import project.bookingservice.spec.BookingInventorySpec;
import project.bookingservice.spec.BookingSpec;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.*;
import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.dto.hotel.internal.*;
import project.commondto.dto.payment.PaymentGateway;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CoreBookingService {

    private final BookingRepo bookingRepo;

    private final HotelClient hotelClient;

    private final BookingDetailRepo bookingDetailRepo;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    private final BookingInventoryRepo bookingInventoryRepo;

    private final BookingMapper bookingMapper;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedissonClient redissonClient;

    public Page<Booking> searchBookings(SearchBookingDto searchBookingDto,
                                        Pageable pageable,
                                        String userId,
                                        String ownerHotelId) {
        Specification<Booking> specBooking = BookingSpec.isDeleted(false)
                .and(BookingSpec.hasPaymentStatus(searchBookingDto.getPaymentStatus()))
                .and(BookingSpec.hasBookingStatus(searchBookingDto.getBookingStatus()))
                .and(BookingSpec.hasKeycloakId(userId))
                .and(BookingSpec.hasHotelId(searchBookingDto.getHotelId()))
                .and(BookingSpec.hasOwnerHotelId(ownerHotelId))
                .and(BookingSpec.hasPaymentId(searchBookingDto.getPaymentId()))
                .and(BookingSpec.sortByCreateDate());
        log.info("Search bookings with: SearchBookingDto: {}, Pageable: {}, OwnerHotelId: {}, UserId: {}", searchBookingDto, pageable, ownerHotelId, userId);
        return bookingRepo.findAll(specBooking, pageable);
    }

    public Page<BookingDetail> searchBookingDetails(String bookingId, Pageable pageable) {

        BaseUtils.validateObject(bookingId, "Booking id", false);

        Specification<BookingDetail> specBookingDetail = BookingDetailSpec.hasBookingId(bookingId)
                .and(BookingDetailSpec.isDeleted(false));
        log.info("Search booking details with: BookingId: {}, Pageable: {}", bookingId, pageable);
        return bookingDetailRepo.findAll(specBookingDetail, pageable);
    }

    public Page<BookingInventory> searchBookingInventories(String bookingDetailId, Pageable pageable) {

        BaseUtils.validateObject(bookingDetailId, "Booking detail id", false);

        Specification<BookingInventory> bookingInventorySpec = BookingInventorySpec.hasBookingDetailId(bookingDetailId)
                .and(BookingInventorySpec.isDeleted(false));
        log.info("Search booking inventories with: BookingDetailId: {}, Pageable: {}", bookingDetailId, pageable);
        return bookingInventoryRepo.findAll(bookingInventorySpec, pageable);
    }

    public Booking processBookings(String userId, CreateBookingDto createBookingDto) {
        BaseUtils.validateObject(userId, "User id", false);
        BaseUtils.validateObject(createBookingDto, "Create booking DTO", false);
        BaseUtils.validateListObject(createBookingDto.getReservedRequests(), "Reserved requests", false);

        Map<String, ReservedRequest> groupedMap = new LinkedHashMap<>();
        for (ReservedRequest request : createBookingDto.getReservedRequests()) {
            String key = buildRequestKey(request.getRoomTypeId(), createBookingDto.getCheckInDate(), createBookingDto.getCheckOutDate());
            groupedMap.merge(key, request, (existing, incoming) -> {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                return existing;
            });
        }
        groupedMap.values().forEach(r -> {
            if (r.getQuantity() > 10) {
                throw new BusinessException("Total quantity for room type " + r.getRoomTypeId() + " exceeds limit of 10");
            }
        });
        createBookingDto.setReservedRequests(new ArrayList<>(groupedMap.values()));

        List<InternalBookingRoomDto> internalBookingRooms = hotelClient.getRoomTypeByIds(createBookingDto);
        BaseUtils.validateListObject(internalBookingRooms, "Room types internal", false);

        if (internalBookingRooms.size() > 1) {
            log.warn("Booking has rooms from multiple hotels: {}", internalBookingRooms.size());
            throw new BusinessException("Booking can only contain rooms from one hotel");
        }

        InternalBookingRoomDto hotelData = internalBookingRooms.getFirst();
        InternalHotelDto hotel = InternalHotelDto.builder()
                .id(hotelData.getId())
                .name(hotelData.getName())
                .urlImage(hotelData.getUrlImage())
                .keycloakId(hotelData.getKeycloakId())
                .build();

        Map<String, InternalRoomTypeDto> roomTypeMap = hotelData.getRoomTypes().stream()
                .collect(Collectors.toMap(InternalRoomTypeDto::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        Map<String, List<BookingInventory>> tempBookingInv = new LinkedHashMap<>();
        Map<String, BookingDetail> tempBookingDetails = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ReservedRequest reqRoom : createBookingDto.getReservedRequests()) {
            InternalRoomTypeDto roomType = roomTypeMap.get(reqRoom.getRoomTypeId());
            BaseUtils.validateObject(roomType, "Room type not found: " + reqRoom.getRoomTypeId(), true);
            BaseUtils.validateObject(roomType.getInventories(), "Inv not enough for room type id: " + reqRoom.getRoomTypeId(), true);

            List<InternalRoomInventoryDto> relevantInventories = roomType.getInventories().stream()
                    .filter(inv -> !inv.getDate().isBefore(createBookingDto.getCheckInDate())
                            && inv.getDate().isBefore(createBookingDto.getCheckOutDate()))
                    .toList();

            long totalNights = ChronoUnit.DAYS.between(createBookingDto.getCheckInDate(), createBookingDto.getCheckOutDate());
            if (totalNights > relevantInventories.size()) {
                log.warn("Not enough available rooms for room type id: {}", reqRoom.getRoomTypeId());
                throw new BusinessException("Not enough available rooms for id: " + reqRoom.getRoomTypeId());
            }

            String requestKey = buildRequestKey(reqRoom.getRoomTypeId(), createBookingDto.getCheckInDate(), createBookingDto.getCheckOutDate());

            for (InternalRoomInventoryDto inventoryDto : relevantInventories) {
                if (inventoryDto.getAvailableQuantity() <= 0 || reqRoom.getQuantity() > inventoryDto.getAvailableQuantity()) {
                    log.warn("Not enough available rooms for room type id: {}", reqRoom.getRoomTypeId());
                    throw new BusinessException("Not enough available rooms for id: " + reqRoom.getRoomTypeId());
                }

                BigDecimal unitPrice = inventoryDto.getPrice();
                BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(reqRoom.getQuantity()));

                BookingInventory bookingInventory = BookingInventory.builder()
                        .date(inventoryDto.getDate())
                        .unitPrice(unitPrice)
                        .allocatedQuantity(reqRoom.getQuantity())
                        .roomInventoryId(inventoryDto.getId())
                        .subTotal(subTotal)
                        .build();

                tempBookingInv.computeIfAbsent(requestKey, k -> new ArrayList<>()).add(bookingInventory);
                totalAmount = totalAmount.add(subTotal);
            }

            BookingDetail bookingDetail = BookingDetail.builder()
                    .roomTypeName(roomType.getName())
                    .roomTypeImageUrl(roomType.getUrlImage() != null ? roomType.getUrlImage().trim() : null)
                    .roomTypeId(roomType.getId())
                    .build();
            tempBookingDetails.put(requestKey, bookingDetail);
        }

        Booking booking = Booking.builder()
                .keycloakId(userId)
                .totalAmount(totalAmount)
                .ownerHotelId(hotel.getKeycloakId())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentId(null)
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelImageUrl(hotel.getUrlImage() != null ? hotel.getUrlImage().trim() : null)
                .checkInDate(createBookingDto.getCheckInDate())
                .checkOutDate(createBookingDto.getCheckOutDate())
                .build();

        String code = null;
        int maxAttempts = 5;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String candidate = generateCode();
            if (!bookingRepo.existsByCode(candidate)) {
                code = candidate;
                break;
            }
        }
        if (code == null) {
            throw new BusinessException("Could not generate a unique booking code, please try again");
        }
        booking.setCode(code);

        Booking bookingSaved = bookingRepo.save(booking);

        tempBookingDetails.values().forEach(detail -> detail.setBookingId(bookingSaved.getId()));

        List<BookingDetail> bookingDetailSaved = bookingDetailRepo.saveAll(new ArrayList<>(tempBookingDetails.values()));

        List<BookingInventory> bookingInventories = new ArrayList<>();
        tempBookingInv.forEach((requestKey, invList) -> {
            String bookingDetailId = tempBookingDetails.get(requestKey).getId();
            invList.forEach(inv -> inv.setBookingDetailId(bookingDetailId));
            bookingInventories.addAll(invList);
        });

        List<BookingInventory> bookingInventorySavedList = bookingInventoryRepo.saveAll(bookingInventories);

        Map<String, List<BookingInventory>> bookingInventoryMap = bookingInventorySavedList.stream()
                .collect(Collectors.groupingBy(BookingInventory::getBookingDetailId, LinkedHashMap::new, Collectors.toList()));

        List<BookingRoomInvDto> bookingRoomInvDtos = bookingDetailSaved.stream()
                .flatMap(detail -> bookingInventoryMap.getOrDefault(detail.getId(), Collections.emptyList()).stream())
                .map(inv -> {
                    BookingRoomInvDto bookingRoomInvDto = BookingRoomInvDto.builder()
                            .id(inv.getRoomInventoryId())
                            .quantity(inv.getAllocatedQuantity())
                            .build();
                    return bookingRoomInvDto;
                })
                .toList();

        ResultBookingsDto resultBookingsDto = ResultBookingsDto.builder()
                .bookingId(bookingSaved.getId())
                .inventories(bookingRoomInvDtos)
                .build();

        createOutboxDecreaseInventories(List.of(resultBookingsDto));

        return bookingSaved;
    }

    public List<BookingDetail> getBookingDetailsByBookings(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        List<String> bookingIds = bookings.stream().distinct().map(Booking::getId).toList();

        return bookingDetailRepo
                .findBookingDetailsByBookingIdsIdAndIsDeleted(bookingIds, false);
    }

    public List<BookingInventory> getBookingInventoriesByBookingDetails(List<BookingDetail> bookingDetails) {
        List<String> bookingDetailIds = bookingDetails.stream()
                .map(BookingDetail::getId)
                .distinct()
                .toList();
        BaseUtils.validateListObject(bookingDetailIds, "Booking Detail IDS", false);
        return bookingInventoryRepo
                .findBookingInventoryByBookingDetailIdsAndIsDeleted(bookingDetailIds, false);

    }

    public void deleteBooking(String bookingId) {
        BaseUtils.validateObject(bookingId, "Booking id", false);

        if (bookingRepo.deletedBooking(bookingId,
                List.of(BookingStatus.CANCELED, BookingStatus.FAILED, BookingStatus.CHECK_OUT)) <= 0) {
            log.warn("Delete booking failed with id: {}", bookingId);
            throw new BusinessException("Booking cannot delete with id: " + bookingId);
        }

        List<String> detailIds = bookingDetailRepo.findIdsByBookingIdAndIsDeleted(bookingId, false);

        if (!detailIds.isEmpty()) {
            bookingDetailRepo.deletedBookingDetailByBookingId(bookingId);
            bookingInventoryRepo.deleteByBookingDetailIds(detailIds);
        }

        log.info("Delete booking and related details success with id: {}", bookingId);
    }

    public List<Booking> confirmBookings(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        BookingStatus newStatus = BookingStatus.CONFIRMED;

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(bookings);

        validateAllBookingsHaveDetail(bookings, bookingDetails, "Confirm booking");

        List<BookingInventory> bookingInventoriesByBookingDetails = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> invalid = bookingInventoriesByBookingDetails.stream()
                .filter(bi -> bi.getStatus() != InventoryStatus.LOCKED)
                .toList();

        if (!invalid.isEmpty()) {
            List<String> invalidIds = invalid.stream().map(BookingInventory::getId).toList();
            log.warn("Booking have not inventory locked with ids: {}", invalidIds);
            throw new BusinessException("Booking have not inventory locked with ids: " + invalidIds);
        }

        bookings.forEach(b -> validateTransition(b.getBookingStatus(), newStatus));

        bookings.forEach(b -> b.setBookingStatus(newStatus));

        InventoryStatus targetInvStatus = TARGET_INVENTORY_STATUS.get(newStatus);
        bookingInventoriesByBookingDetails.forEach(i -> i.setStatus(targetInvStatus));

        Map<String, List<BookingDetail>> mapBookingDetails = bookingDetails.stream()
                .collect(Collectors.groupingBy(BookingDetail::getBookingId, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<BookingInventory>> mapBookingInvs = bookingInventoriesByBookingDetails.stream()
                .collect(Collectors.groupingBy(BookingInventory::getBookingDetailId, LinkedHashMap::new, Collectors.toList()));

        List<OutBoxDto> outBoxDtos = new ArrayList<>();

        bookings.forEach(b -> {
            BookingResponse bookingResponse = publishOutboxBookingResponse(mapBookingDetails, mapBookingInvs, b);
            OutBoxDto outBoxDto = OutBoxDto.builder()
                    .payload(BaseUtils.convertObjectToString(objectMapper, bookingResponse))
                    .topic(KafkaTopic.NOTIFICATION_BOOKING_CONFIRMED)
                    .build();
            outBoxDtos.add(outBoxDto);
        });

        outboxService.createOutboxes(outBoxDtos);
        return bookings;
    }

    public List<Booking> getBookings(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        return bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
    }

    public List<Booking> cancelBookings(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        BookingStatus newStatus = BookingStatus.CANCELED;

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(bookings);
        validateAllBookingsHaveDetail(bookings, bookingDetails, "Cancel booking");

        List<BookingInventory> inventories = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> stillPending = inventories.stream()
                .filter(bi -> bi.getStatus() == InventoryStatus.NEW)
                .toList();

        if (!stillPending.isEmpty()) {
            List<String> ids = stillPending.stream().map(BookingInventory::getId).toList();
            log.warn("Cannot cancel booking while inventory not yet confirmed by room: {}", ids);
            throw new BusinessException("Cannot cancel booking while inventory not yet confirmed by room: " + ids);
        }

        List<Booking> needRefundOnline = bookings.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && !b.getPaymentMethod().equals(PaymentMethod.CASH))
                .toList();

        List<Booking> needRefundCash = bookings.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID && b.getPaymentMethod().equals(PaymentMethod.CASH))
                .toList();

        bookings.forEach(b -> validateTransition(b.getBookingStatus(), newStatus));

        needRefundOnline.forEach(b -> validatePaymentTransition(b.getPaymentStatus(), PaymentStatus.PROCESSING_REFUND));
        needRefundCash.forEach(b -> validatePaymentTransition(b.getPaymentStatus(), PaymentStatus.REFUNDED));

        bookings.forEach(b -> b.setBookingStatus(newStatus));

        List<BookingInventory> wasReserved = inventories.stream()
                .filter(bi -> RESERVED_STATES.contains(bi.getStatus()))
                .toList();
        if (!wasReserved.isEmpty()) {
            publishIncreaseInventoryOutbox(wasReserved, bookingDetails);
        }

        if (!needRefundOnline.isEmpty()) {
            createOutboxRefundPayment(needRefundOnline);
            needRefundOnline.forEach(b -> b.setPaymentStatus(PaymentStatus.PROCESSING_REFUND));
        }
        if (!needRefundCash.isEmpty()) {
            needRefundCash.forEach(b -> b.setPaymentStatus(PaymentStatus.REFUNDED));
        }

        Map<String, List<BookingDetail>> mapBookingDetails = bookingDetails.stream()
                .collect(Collectors.groupingBy(BookingDetail::getBookingId, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<BookingInventory>> mapBookingInvs = inventories.stream()
                .collect(Collectors.groupingBy(BookingInventory::getBookingDetailId, LinkedHashMap::new, Collectors.toList()));
        List<OutBoxDto> outBoxDtos = new ArrayList<>();

        bookings.forEach(b -> {
            BookingResponse bookingResponse = publishOutboxBookingResponse(mapBookingDetails, mapBookingInvs, b);
            OutBoxDto outBoxDto = OutBoxDto.builder()
                    .payload(BaseUtils.convertObjectToString(objectMapper, bookingResponse))
                    .topic(KafkaTopic.NOTIFICATION_BOOKING_CANCELED)
                    .build();
            outBoxDtos.add(outBoxDto);
        });

        outboxService.createOutboxes(outBoxDtos);
        return bookings;
    }

    private BookingResponse publishOutboxBookingResponse(Map<String, List<BookingDetail>> mapBookingDetails, Map<String, List<BookingInventory>> mapBookingInvs, Booking b) {
        BookingResponse bookingResponse = BookingResponse.builder()
                .common(bookingMapper.toDto(b))
                .build();
        List<InternalFullRoomTypeDto> internalFullRoomTypeDtos = new ArrayList<>();
        mapBookingDetails.get(b.getId()).forEach(bt -> {
            InternalFullRoomTypeDto internalFullRoomTypeDto = InternalFullRoomTypeDto
                    .builder()
                    .id(bt.getRoomTypeId())
                    .name(bt.getRoomTypeName())
                    .urlImage(bt.getRoomTypeImageUrl())
                    .build();
            List<InternalRoomInventoryDto> internalRoomInventoryDtos = new ArrayList<>();
            mapBookingInvs.get(bt.getId()).forEach(inv -> {
                InternalRoomInventoryDto internalRoomInventoryDto = InternalRoomInventoryDto
                        .builder()
                        .id(inv.getRoomInventoryId())
                        .price(inv.getUnitPrice())
                        .date(inv.getDate())
                        .availableQuantity(inv.getAllocatedQuantity())
                        .build();
                internalRoomInventoryDtos.add(internalRoomInventoryDto);
            });
            internalFullRoomTypeDto.setInventories(internalRoomInventoryDtos);
            internalFullRoomTypeDtos.add(internalFullRoomTypeDto);
        });
        bookingResponse.setDetails(internalFullRoomTypeDtos);
        return bookingResponse;
    }

    public void checkIn(Booking booking) {
        BaseUtils.validateObject(booking, "Booking", false);
        if (LocalDate.now().isAfter(booking.getCheckOutDate())) {
            throw new BusinessException("Cannot check-in booking because it has expired!");
        }
        if (!booking.getPaymentStatus().equals(PaymentStatus.PAID)) {
            throw new BusinessException("Cannot check-in booking because not paid!");
        }
        validateTransition(booking.getBookingStatus(), BookingStatus.CHECK_IN);
        booking.setBookingStatus(BookingStatus.CHECK_IN);
        booking.setRealCheckInDate(LocalDate.now());
    }

    public void checkOut(Booking booking) {
        BaseUtils.validateObject(booking, "Booking", false);
        validateTransition(booking.getBookingStatus(), BookingStatus.CHECK_OUT);

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(List.of(booking));
        validateAllBookingsHaveDetail(List.of(booking), bookingDetails, "Check out booking");

        List<BookingInventory> inventories = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> notCommittedYet = inventories.stream()
                .filter(bi -> bi.getStatus() != InventoryStatus.COMMITED)
                .toList();

        if (!notCommittedYet.isEmpty()) {
            List<String> ids = notCommittedYet.stream().map(BookingInventory::getId).toList();
            log.warn("Cannot check-out booking while inventory not yet committed: {}", ids);
            throw new BusinessException("Cannot check-out booking while inventory not yet confirmed by room: " + ids);
        }

        publishIncreaseInventoryOutbox(inventories, bookingDetails);

        booking.setBookingStatus(BookingStatus.CHECK_OUT);
        booking.setRealCheckOutDate(LocalDate.now());
    }

    public List<Booking> failBookings(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        BookingStatus newStatus = BookingStatus.FAILED;

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(bookings);

        validateAllBookingsHaveDetail(bookings, bookingDetails, "Fail booking");

        List<BookingInventory> inventories = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> invalid = inventories.stream()
                .filter(bi -> bi.getStatus() != InventoryStatus.NEW)
                .toList();

        if (!invalid.isEmpty()) {
            List<String> invalidIds = invalid.stream().map(BookingInventory::getId).toList();
            log.warn("Cannot fail booking, inventory already reserved: {}", invalidIds);
            throw new BusinessException("Cannot fail booking, inventory already reserved: " + invalidIds);
        }

        bookings.forEach(b -> validateTransition(b.getBookingStatus(), newStatus));
        bookings.forEach(b -> b.setBookingStatus(newStatus));

        inventories.forEach(bi -> bi.setStatus(InventoryStatus.RELEASED));

        return bookings;
    }

    public List<BookingInventory> lockInventoriesBooking(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(bookings);

        validateAllBookingsHaveDetail(bookings, bookingDetails, "Lock inv");

        List<BookingInventory> inventories = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> invalid = inventories.stream()
                .filter(bi -> bi.getStatus() != InventoryStatus.NEW)
                .toList();

        if (!invalid.isEmpty()) {
            List<String> invalidIds = invalid.stream().map(BookingInventory::getId).toList();
            log.warn("Cannot lock inventory not in NEW state: {}", invalidIds);
            throw new BusinessException("Cannot lock inventory not in NEW state: " + invalidIds);
        }

        inventories.forEach(bi -> bi.setStatus(InventoryStatus.LOCKED));

        return inventories;
    }

    public List<BookingInventory> syncInventoryReleased(List<Booking> bookings) {

        BaseUtils.validateListObject(bookings, "Bookings", false);

        List<BookingDetail> bookingDetails = getBookingDetailsByBookings(bookings);

        validateAllBookingsHaveDetail(bookings, bookingDetails, "Sync release inv");

        List<BookingInventory> inventories = getBookingInventoriesByBookingDetails(bookingDetails);

        List<BookingInventory> invalid = inventories.stream()
                .filter(bi -> !RESERVED_STATES.contains(bi.getStatus()))
                .toList();

        if (!invalid.isEmpty()) {
            List<String> invalidIds = invalid.stream().map(BookingInventory::getId).toList();
            log.warn("Received release confirmation for inventory not in reserved state: {}", invalidIds);
            throw new BusinessException("Cannot release inventory not in reserved state: " + invalidIds);
        }

        inventories.forEach(bi -> bi.setStatus(InventoryStatus.RELEASED));

        return inventories;
    }

    public Booking getBookingFromOption(String bookingId, Optional<Booking> opBooking) {

        BaseUtils.validateObject(bookingId, "Booking id", false);

        if (opBooking.isEmpty()) {
            log.warn("Booking not found with id:  {}", bookingId);
            throw new BusinessException("Booking not found with id: " + bookingId);
        }
        Booking booking = opBooking.get();
        log.warn("Booking founded with id:  {}", booking.getId());
        return booking;

    }

    public BookingDetail getBookingDetailFromOption(String bookingDetailId, Optional<BookingDetail> opBookingDetail) {

        BaseUtils.validateObject(bookingDetailId, "Booking Detail id", false);

        if (opBookingDetail.isEmpty()) {
            log.warn("Booking detail not found with id:  {}", bookingDetailId);
            throw new BusinessException("Booking detail not found with id: " + bookingDetailId);
        }
        BookingDetail bookingDetail = opBookingDetail.get();
        log.info("Booking detail founded with id:  {}", bookingDetail.getId());
        return bookingDetail;

    }

    public void existsBookingByBookingIdAndUserId(String bookingId, String userId) {

        BaseUtils.validateObject(userId, "User id", false);
        BaseUtils.validateObject(bookingId, "Booking id", false);

        boolean exists = bookingRepo.existsByIdAndKeycloakIdAndIsDeleted(bookingId, userId, false);
        if (!exists) {
            log.warn("Booking not exists with id:  {}", bookingId);
            throw new BusinessException("Booking not exists with id: " + bookingId);
        }
    }

    //    public List<BookingDto> addPaymentInfoBooking(List<BookingPaymentDto> bookingPaymentsDto) {
//        BaseUtils.validateListObject(bookingPaymentsDto, "Booking Payments DTO", false);
//
//        List<String> bookingIds = bookingPaymentsDto.stream()
//                .map(BookingPaymentDto::getBookingId)
//                .distinct()
//                .toList();
//
//        List<Booking> bookings = bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
//        Map<String, Booking> bookingMap = bookings.stream()
//                .collect(Collectors.toMap(Booking::getId, b -> b));
//
//        List<String> missingBookingIds = bookingIds.stream()
//                .filter(id -> !bookingMap.containsKey(id))
//                .toList();
//        if (!missingBookingIds.isEmpty()) {
//            throw new BusinessException("Booking not found for payment update, ids: " + missingBookingIds);
//        }
//
//        List<Booking> bookingUpdated = new ArrayList<>();
//
//        bookingPaymentsDto.forEach(dto -> {
//            Booking booking = bookingMap.get(dto.getBookingId());
//            coreBookingService.updateStatusPaymentBooking(booking, dto.getPaymentStatus());
//            booking.setPaymentId(dto.getPaymentId());
//            booking.setPaymentMethod(dto.getPaymentMethod());
//            booking.setPaymentGateway(dto.getPaymentGateway());
//            bookingUpdated.add(booking);
//        });
//
//        return bookingUpdated.stream().map(bookingMapper::toDto).toList();
//    }
    public Booking updatePaymentBooking(Booking booking,
                                        PaymentGateway paymentGateway,
                                        PaymentMethod paymentMethod,
                                        PaymentStatus newPaymentStatus) {
        return updatePaymentBooking(booking, null, paymentGateway, paymentMethod, newPaymentStatus);
    }

    public Booking addPaymentBooking(Booking booking, String paymentId,
                                     PaymentGateway paymentGateway,
                                     PaymentMethod paymentMethod,
                                     PaymentStatus newPaymentStatus) {
        BaseUtils.validateObject(paymentId, "Payment id", false);
        Booking bookingUpdated = updatePaymentBooking(booking, paymentId, paymentGateway, paymentMethod, newPaymentStatus);
        return bookingUpdated;
    }

    private Booking updatePaymentBooking(Booking booking, String paymentId,
                                         PaymentGateway paymentGateway,
                                         PaymentMethod paymentMethod,
                                         PaymentStatus newPaymentStatus) {
        BaseUtils.validateObject(booking, "Booking", false);
        BaseUtils.validateObject(paymentGateway, "Payment gateway", false);
        BaseUtils.validateObject(paymentMethod, "Payment method", false);
        BaseUtils.validateObject(newPaymentStatus, "New payment status", false);
        BaseUtils.validateObject(booking.getPaymentStatus(), "Current payment status", false);

        PaymentStatus currentStatus = booking.getPaymentStatus();
        validatePaymentTransition(currentStatus, newPaymentStatus);

        booking.setPaymentStatus(newPaymentStatus);
        if (paymentId != null) {
            booking.setPaymentId(paymentId);
        }
        booking.setPaymentGateway(paymentGateway);
        booking.setPaymentMethod(paymentMethod);

        log.info("Update payment status {} -> {} success with booking id: {}",
                currentStatus, newPaymentStatus, booking.getId());

        return booking;
    }


    // -------------------------- PR ------------------------------------------------------------------
    private void createOutboxDecreaseInventories(List<ResultBookingsDto> resultBookingsDtos) {
        BaseUtils.validateListObject(resultBookingsDtos, "Result bookings DTO", false);
        String stringBookingRes = BaseUtils.convertObjectToString(objectMapper, resultBookingsDtos);
        outboxService.createOutbox(OutBoxDto.builder()
                .payload(stringBookingRes)
                .topic(KafkaTopic.BOOKING_CREATED)
                .build());
    }

    private void createOutboxIncreaseInventories(List<ResultBookingsDto> resultBookingsDtos) {
        BaseUtils.validateListObject(resultBookingsDtos, "Result bookings DTO", false);
        String stringBookingRes = BaseUtils.convertObjectToString(objectMapper, resultBookingsDtos);
        outboxService.createOutbox(OutBoxDto.builder()
                .payload(stringBookingRes)
                .topic(KafkaTopic.ROOM_PROCESSING_INCREASE)
                .build());

    }

    private void createOutboxRefundPayment(List<Booking> bookings) {
        BaseUtils.validateListObject(bookings, "Bookings", false);

        List<BookingPaymentDto> bookingPaymentsDto = bookings.stream()
                .filter(b -> b.getPaymentId() != null
                        && !b.getPaymentId().isEmpty()
                        && b.getPaymentStatus() == PaymentStatus.PAID)
                .map(bookingMapper::toPaymentDto)
                .distinct()
                .toList();

        if (bookingPaymentsDto.isEmpty()) {
            log.info("No paid bookings to refund, skip creating outbox");
            return;
        }

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .topic(KafkaTopic.BOOKING_CANCELED)
                .payload(BaseUtils.convertObjectToString(objectMapper, bookingPaymentsDto))
                .build();
        outboxService.createOutbox(outBoxDto);
    }

    private String buildRequestKey(String roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return roomTypeId + "|" + checkIn + "|" + checkOut;
    }

//    private void validateNoOverlappingDateRanges(LocalDate checkIn, LocalDate checkOut,List<ReservedRequest> requests) {
//        Map<String, List<ReservedRequest>> byRoomType = requests.stream()
//                .collect(Collectors.groupingBy(ReservedRequest::getRoomTypeId));
//
//        byRoomType.forEach((roomTypeId, reqList) -> {
//            List<ReservedRequest> sorted = reqList.stream()
//                    .sorted(Comparator.comparing(ReservedRequest::getCheckInDate))
//                    .toList();
//
//            for (int i = 1; i < sorted.size(); i++) {
//                ReservedRequest prev = sorted.get(i - 1);
//                ReservedRequest curr = sorted.get(i);
//
//                if (curr.getCheckInDate().isBefore(prev.getCheckOutDate())) {
//                    throw new BusinessException("Overlapping date range for room type id: " + roomTypeId);
//                }
//            }
//        });
//    }

    private void handleErrorMessageStatus(String currentStatus, String newStatus) {
        log.warn("Invalid booking status transition: {} -> {}", currentStatus, newStatus);
        throw new BusinessException("Invalid booking status transition: " + currentStatus + " -> " + newStatus);
    }

    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = Map.of(
            BookingStatus.PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELED, BookingStatus.FAILED),
            BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.CHECK_IN, BookingStatus.CANCELED),
            BookingStatus.CHECK_IN, EnumSet.of(BookingStatus.CHECK_OUT)
    );

    private static final Map<PaymentStatus, Set<PaymentStatus>> PAYMENT_VALID_TRANSITIONS = Map.of(
            PaymentStatus.UNPAID, EnumSet.of(PaymentStatus.PROCESSING_PAID, PaymentStatus.UNPAID),
            PaymentStatus.PROCESSING_PAID, EnumSet.of(PaymentStatus.PAID, PaymentStatus.PAID_FAILED, PaymentStatus.UNPAID),
            PaymentStatus.PAID_FAILED, EnumSet.of(PaymentStatus.PROCESSING_PAID),
            PaymentStatus.PAID, EnumSet.of(PaymentStatus.PROCESSING_REFUND,PaymentStatus.REFUNDED),
            PaymentStatus.PROCESSING_REFUND, EnumSet.of(PaymentStatus.REFUNDED, PaymentStatus.REFUND_FAILED)
    );

    private static final Map<BookingStatus, InventoryStatus> TARGET_INVENTORY_STATUS = Map.of(
            BookingStatus.PENDING, InventoryStatus.LOCKED,
            BookingStatus.CONFIRMED, InventoryStatus.COMMITED,
            BookingStatus.CHECK_IN, InventoryStatus.COMMITED,
            BookingStatus.CANCELED, InventoryStatus.RELEASED,
            BookingStatus.FAILED, InventoryStatus.RELEASED,
            BookingStatus.CHECK_OUT, InventoryStatus.RELEASED
    );

    public void validatePaymentTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        Set<PaymentStatus> allowed = PAYMENT_VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            log.warn("Invalid payment status transition: {} -> {}", currentStatus, newStatus);
            throw new BusinessException("Invalid payment status transition: " + currentStatus + " -> " + newStatus);
        }
    }

    private static final Set<InventoryStatus> RESERVED_STATES = EnumSet.of(InventoryStatus.LOCKED, InventoryStatus.COMMITED);

    private void validateTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        Set<BookingStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            handleErrorMessageStatus(currentStatus.name(), newStatus.name());
        }
    }

    private void validateAllBookingsHaveDetail(List<Booking> bookings, List<BookingDetail> bookingDetails, String action) {
        Set<String> bookingIdsWithDetail = bookingDetails.stream()
                .map(BookingDetail::getBookingId)
                .collect(Collectors.toSet());

        List<String> missingDetailIds = bookings.stream()
                .map(Booking::getId)
                .filter(id -> !bookingIdsWithDetail.contains(id))
                .toList();

        if (!missingDetailIds.isEmpty()) {
            log.warn("Booking has no detail/inventory to {}: {}", action, missingDetailIds);
            throw new BusinessException("Booking has no detail/inventory to " + action + ": " + missingDetailIds);
        }
    }

    private void publishIncreaseInventoryOutbox(List<BookingInventory> reservedInventories,
                                                List<BookingDetail> bookingDetails) {
        Map<String, String> bookingDetailMap = new HashMap<>();
        bookingDetails.forEach(bd -> bookingDetailMap.putIfAbsent(bd.getId(), bd.getBookingId()));

        Map<String, List<BookingRoomInvDto>> bookingInvMap = new HashMap<>();
        reservedInventories.forEach(bi -> {
            String bookingId = bookingDetailMap.get(bi.getBookingDetailId());
            BookingRoomInvDto bookingRoomInvDto = BookingRoomInvDto.builder()
                    .id(bi.getRoomInventoryId())
                    .quantity(bi.getAllocatedQuantity())
                    .build();
            bookingInvMap.computeIfAbsent(bookingId, k -> new ArrayList<>()).add(bookingRoomInvDto);
        });

        List<ResultBookingsDto> resultBookingsDtos = bookingInvMap.entrySet().stream()
                .map(e -> {
                    ResultBookingsDto resultBookingsDto = ResultBookingsDto.builder()
                            .bookingId(e.getKey())
                            .inventories(e.getValue())
                            .build();
                    return resultBookingsDto;
                })
                .toList();

        createOutboxIncreaseInventories(resultBookingsDtos);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public <T> T withBookingLocks(List<String> bookingIds, Supplier<T> action) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        RLock[] lockBookings = bookingIds.stream()
                .distinct()
                .sorted()
                .map(id -> BaseUtils.getLock(redissonClient, "booking", id))
                .toArray(RLock[]::new);

        RLock multiLock = BaseUtils.getMultiLock(redissonClient, Arrays.asList(lockBookings));

        boolean locked;
        try {
            locked = multiLock.tryLock(5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring lock for bookings: {}", bookingIds, e);
            throw new BusinessException("Server busy, try again later");
        }

        if (!locked) {
            log.warn("Could not acquire lock for bookings: {}, will not retry", bookingIds);
            throw new BusinessException("Server busy, try again later");
        }

        try {
            return action.get();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("When processing bookings: {} appearance error: {}", bookingIds, e.getMessage(), e);
            throw new BusinessException("Server busy, try again later");
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                log.info("Lock: {} is released for bookings: {}", multiLock, bookingIds);
                multiLock.unlock();
            }
        }
    }
}