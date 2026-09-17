package project.hotelservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.ReservedRequest;
import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.dto.hotel.RoomInventoryUpdateAction;
import project.commondto.dto.hotel.dto.RoomInventoryDto;
import project.commondto.dto.hotel.internal.InternalBookingRoomDto;
import project.commondto.dto.hotel.internal.InternalRoomInventoryDto;
import project.commondto.dto.hotel.internal.InternalRoomTypeDto;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.entity.RoomType;
import project.hotelservice.mapper.RoomInventoryMapper;
import project.hotelservice.mapper.RoomTypeMapper;
import project.hotelservice.repo.HotelRepo;
import project.hotelservice.repo.RoomInventoryRepo;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.InternalService;
import project.hotelservice.service.core.CoreRoomInventoryService;
import project.hotelservice.spec.HotelSpec;
import project.hotelservice.spec.RoomInventorySpec;
import project.hotelservice.spec.RoomTypeSpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InternalServiceImpl implements InternalService {

    private final RoomTypeRepo roomTypeRepo;

    private final RoomInventoryRepo roomInventoryRepo;

    private final RoomTypeMapper roomTypeMapper;

    private final RoomInventoryMapper roomInventoryMapper;

    private final CoreRoomInventoryService coreRoomInventoryService;

    private final HotelRepo hotelRepo;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Override
    public List<InternalBookingRoomDto> getRoomTypeByIds(CreateBookingDto createBookingDto) {

        BaseUtils.validateObject(createBookingDto, "Create booking", false);
        BaseUtils.validateObject(createBookingDto.getCheckInDate(),"Check-in date",false);
        BaseUtils.validateObject(createBookingDto.getCheckOutDate(),"Check-out date",false);
        BaseUtils.validateListObject(createBookingDto.getReservedRequests(), "Reserved request", false);

        List<Specification<RoomInventory>> orSpecs = createBookingDto.getReservedRequests().stream()
                .map(req -> RoomInventorySpec.hasRoomTypeId(req.getRoomTypeId())
                        .and(RoomInventorySpec.isDateBetween(createBookingDto.getCheckInDate(), createBookingDto.getCheckOutDate())))
                .toList();

        Specification<RoomInventory> combinedOrSpec = Specification.anyOf(orSpecs);
        Specification<RoomInventory> finalSpec = combinedOrSpec.and(RoomInventorySpec.isDeleted(false));

        List<RoomInventory> roomInventories = roomInventoryRepo.findAll(finalSpec);

        List<String> roomTypeIds = roomInventories.stream()
                .map(RoomInventory::getRoomTypeId)
                .distinct()
                .toList();

        Map<String, List<RoomInventory>> invMap = roomInventories.stream()
                .collect(Collectors.groupingBy(RoomInventory::getRoomTypeId));

        Specification<RoomType> roomTypeSpecification = RoomTypeSpec.hasIds(roomTypeIds)
                .and(RoomTypeSpec.isApproval(true))
                .and(RoomTypeSpec.isDeleted(false));

        List<RoomType> roomTypes = roomTypeRepo.findAll(roomTypeSpecification);

        List<String> hotelIds = roomTypes.stream()
                .map(RoomType::getHotelId)
                .distinct()
                .toList();

        Map<String, List<RoomType>> roomTypeMap = roomTypes.stream()
                .collect(Collectors.groupingBy(RoomType::getHotelId));

        Specification<Hotel> hotelSpecification = HotelSpec.hasIds(hotelIds)
                .and(HotelSpec.isApproval(true))
                .and(HotelSpec.isDeleted(false));

        return hotelRepo.findAll(hotelSpecification).stream()
                .map(hotel -> {
                    List<RoomType> currentHotelRoomTypes = roomTypeMap.getOrDefault(hotel.getId(), Collections.emptyList());

                    List<InternalRoomTypeDto> itemRoomTypes = currentHotelRoomTypes.stream()
                            .map(roomType -> {
                                List<InternalRoomInventoryDto> itemRoomInv = invMap.getOrDefault(roomType.getId(), Collections.emptyList())
                                        .stream()
                                        .map(roomInventoryMapper::toInternalDto)
                                        .toList();

                                InternalRoomTypeDto roomTypeDto = roomTypeMapper.toInternalDto(roomType);
                                roomTypeDto.setInventories(itemRoomInv);
                                return roomTypeDto;
                            })
                            .toList();

                    InternalBookingRoomDto internalBookingRoomDto = InternalBookingRoomDto.builder()
                            .id(hotel.getId())
                            .name(hotel.getName())
                            .urlImage(hotel.getImageUri() + "/" + hotel.getImageName())
                            .keycloakId(hotel.getKeycloakId())
                            .roomTypes(itemRoomTypes)
                            .build();
                    return internalBookingRoomDto;
                }).toList();
    }

    @Transactional
    @Override
    public void decreaseQuantity(List<String> bookingIds, List<BookingRoomInvDto> bookingRoomInvDtos) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        BaseUtils.validateListObject(bookingRoomInvDtos, "Booking room inventories DTO", false);

        List<RoomInventoryDto> roomInventories = bookingRoomInvDtos.stream().map(i -> {
            RoomInventoryDto roomInventoryDto = RoomInventoryDto.builder()
                    .id(i.getId())
                    .availableQuantity(i.getQuantity())
                    .build();
            return roomInventoryDto;
        }).toList();
        coreRoomInventoryService.updateQuantityRoomInventory(roomInventories, RoomInventoryUpdateAction.HOLD);

        List<ResultBookingsDto> resultBookingsDtos = bookingIds.stream().map(i -> {
            ResultBookingsDto resultBookingsDto = ResultBookingsDto
                    .builder()
                    .bookingId(i)
                    .build();
            return resultBookingsDto;
        }).toList();

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .topic(KafkaTopic.ROOM_DECREASED)
                .payload(BaseUtils.convertObjectToString(objectMapper, resultBookingsDtos))
                .build();

        outboxService.createOutbox(outBoxDto);
    }

    @Transactional
    @Override
    public void increaseQuantity(List<String> bookingIds, List<BookingRoomInvDto> bookingRoomInvDtos) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        BaseUtils.validateListObject(bookingRoomInvDtos, "Booking room inventories DTO", false);

        List<RoomInventoryDto> roomInventories = bookingRoomInvDtos.stream().map(i -> {
            RoomInventoryDto roomInventoryDto = RoomInventoryDto.builder()
                    .id(i.getId())
                    .availableQuantity(i.getQuantity())
                    .build();
            return roomInventoryDto;
        }).toList();
        coreRoomInventoryService.updateQuantityRoomInventory(roomInventories, RoomInventoryUpdateAction.REVERT);

        List<ResultBookingsDto> resultBookingsDtos = bookingIds.stream().map(i -> {
            ResultBookingsDto resultBookingsDto = ResultBookingsDto
                    .builder()
                    .bookingId(i)
                    .build();
            return resultBookingsDto;
        }).toList();

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .topic(KafkaTopic.ROOM_INCREASED)
                .payload(BaseUtils.convertObjectToString(objectMapper, resultBookingsDtos))
                .build();

        outboxService.createOutbox(outBoxDto);
    }

}
