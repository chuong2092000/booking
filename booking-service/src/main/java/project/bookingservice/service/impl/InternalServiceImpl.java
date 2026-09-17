package project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookingservice.entity.Booking;
import project.bookingservice.entity.BookingDetail;
import project.bookingservice.entity.BookingInventory;
import project.bookingservice.job.BookingExpirationScheduler;
import project.bookingservice.mapper.BookingInventoryMapper;
import project.bookingservice.mapper.BookingMapper;
import project.bookingservice.repo.BookingRepo;
import project.bookingservice.service.InternalService;
import project.bookingservice.service.core.CoreBookingService;
import project.commondto.dto.booking.BookingDto;
import project.commondto.dto.booking.BookingInventoryDto;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.booking.InventoryStatus;
import project.commondto.dto.payment.PaymentStatus;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InternalServiceImpl implements InternalService {

    private final CoreBookingService coreBookingService;

    private final BookingRepo bookingRepo;

    private final BookingMapper bookingMapper;

    private final BookingInventoryMapper bookingInventoryMapper;

    private final BookingExpirationScheduler bookingExpirationScheduler;

    @Transactional
    @Override
    public List<BookingDto> cancelBooking(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
        validBookingsSize(bookings, bookingIds);

        return coreBookingService.cancelBookings(bookings).stream().map(bookingMapper::toDto).toList();
    }

    @Transactional
    @Override
    public List<BookingDto> failBooking(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
        validBookingsSize(bookings, bookingIds);

        return coreBookingService.failBookings(bookings).stream().map(bookingMapper::toDto).toList();
    }

    @Transactional
    @Override
    public List<BookingInventoryDto> lockBookingInventories(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
        validBookingsSize(bookings, bookingIds);

        return coreBookingService.lockInventoriesBooking(bookings).stream().map(bookingInventoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public List<BookingInventoryDto> releaseBookingInventories(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndIsDeleted(bookingIds, false);
        validBookingsSize(bookings, bookingIds);

        return coreBookingService.syncInventoryReleased(bookings).stream().map(bookingInventoryMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getBookingsHaveBookingInventoriesLockedByBookingIds(List<String> bookingIds) {

        List<Booking> bookings = bookingRepo.getBookingsByIdWithStatus(
                bookingIds, false, PaymentStatus.UNPAID, BookingStatus.PENDING);

        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Booking> bookingsMap = bookings.stream()
                .collect(Collectors.toMap(Booking::getId, b -> b, (oldValue, newValue) -> oldValue));

        List<BookingDetail> bookingDetails = coreBookingService.getBookingDetailsByBookings(bookings);

        Map<String, List<String>> bookingIdToDetailIds = bookingDetails.stream()
                .collect(Collectors.groupingBy(
                        BookingDetail::getBookingId,
                        Collectors.mapping(BookingDetail::getId, Collectors.toList())
                ));

        List<BookingInventory> bookingInventories = coreBookingService.getBookingInventoriesByBookingDetails(bookingDetails);

        Map<String, List<BookingInventory>> bookingInventoriesMap = bookingInventories.stream()
                .collect(Collectors.groupingBy(BookingInventory::getBookingDetailId));

        Set<String> fullyLockedDetailIds = bookingInventoriesMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty()
                        && entry.getValue().stream().allMatch(i -> InventoryStatus.LOCKED.equals(i.getStatus())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Booking> results = bookingIdToDetailIds.entrySet().stream()
                .filter(entry -> fullyLockedDetailIds.containsAll(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(bookingsMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return results.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void cleanBookingPaymentInfo(String bookingId) {
        BaseUtils.validateObject(bookingId, "Booking id", false);
        Optional<Booking> optionalBooking = bookingRepo.getBookingByIdAndIsDeleted(bookingId, false);
        Booking booking = coreBookingService.getBookingFromOption(bookingId, optionalBooking);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            log.warn("Booking {} is not PENDING (current: {}), skip cleaning payment info",
                    bookingId, booking.getBookingStatus());
            return;
        }
        if (booking.getPaymentStatus() != PaymentStatus.PROCESSING_PAID) {
            log.warn("Booking {} payment status is {} (expected PROCESSING_PAID), skip cleaning",
                    bookingId, booking.getPaymentStatus());
            return;
        }

        booking.setPaymentId(null);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setPaymentMethod(null);

        log.info("Cleaned payment info for booking {}, re-scheduling room-hold", bookingId);

        Instant newRoomHoldExpireAt = Instant.now().plus(17, ChronoUnit.MINUTES);
        bookingExpirationScheduler.scheduleRoomHold(bookingId, newRoomHoldExpireAt);
    }

    private void validBookingsSize(List<Booking> bookings, List<String> bookingIds) {
        if (bookings.size() != bookingIds.size()) {
            log.warn("Have one or more booking not found in list id: {}", bookingIds);
            throw new BusinessException("Booking not found with list id: " + bookingIds);
        }
    }

}