package project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookingservice.entity.Booking;
import project.bookingservice.mapper.BookingDetailMapper;
import project.bookingservice.mapper.BookingMapper;
import project.bookingservice.orchestrator.OrchestratorService;
import project.bookingservice.repo.BookingRepo;
import project.bookingservice.service.OwnerBookingService;
import project.bookingservice.service.core.CoreBookingService;
import project.commondto.dto.booking.BookingDetailDto;
import project.commondto.dto.booking.OwnerBookingDto;
import project.commondto.dto.booking.SearchBookingDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerBookingServiceImpl implements OwnerBookingService {

    private final BookingRepo bookingRepo;

    private final CoreBookingService coreBookingService;

    private final BookingMapper bookingMapper;

    private final BookingDetailMapper bookingDetailMapper;

    private final OrchestratorService orchestratorService;

    @Override
    public List<OwnerBookingDto> cancelBooking(String ownerHotelId, List<String> bookingIds) {

        validateOwnerHotelAndBookingIds(ownerHotelId, bookingIds);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndOwnerIdAndIsDeleted(bookingIds, ownerHotelId, false);

        validBookingsSize(bookings, bookingIds);

        return coreBookingService.withBookingLocks(bookingIds, () ->
                orchestratorService.cancelBookings(bookings).stream().map(bookingMapper::toOwnerDto).toList());
    }

    @Transactional(readOnly = true)
    @Override
    public OwnerBookingDto getBooking(String ownerHotelId, String bookingId) {

        validateOwnerHotelAndBookingId(ownerHotelId, bookingId);

        Optional<Booking> opBooking = bookingRepo.getBookingByIdAndOwnerHotelIdAndIsDeleted(bookingId, ownerHotelId, false);

        return bookingMapper.toOwnerDto(
                coreBookingService.getBookingFromOption(bookingId, opBooking));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookingDetailDto> getBookingDetails(String ownerHotelId, String bookingId, Pageable pageable) {

        validateOwnerHotelAndBookingId(ownerHotelId, bookingId);

        boolean exists = bookingRepo.existsByIdAndOwnerHotelIdAndIsDeleted(bookingId, ownerHotelId, false);
        if (!exists) {
            log.warn("Booking not found with id:  {}", bookingId);
            throw new BusinessException("Booking not found with id: " + bookingId);
        }

        return coreBookingService.searchBookingDetails(bookingId, pageable).map(bookingDetailMapper::toDto);
    }

    @Transactional
    @Override
    public void checkInBooking(String ownerHotelId, String code) {
        Booking booking = getBookingByCodeAndOwnerHotelId(ownerHotelId, code);
        coreBookingService.checkIn(booking);
    }

    @Transactional
    @Override
    public void checkOutBooking(String ownerHotelId, String code) {
        Booking booking = getBookingByCodeAndOwnerHotelId(ownerHotelId, code);
        coreBookingService.checkOut(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OwnerBookingDto> getBookings(SearchBookingDto searchBookingDto, Pageable pageable, String userId, String ownerHotelId) {
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        return coreBookingService.searchBookings(searchBookingDto, pageable, userId, ownerHotelId).map(bookingMapper::toOwnerDto);
    }

    private void validateOwnerHotelAndBookingId(String ownerHotelId, String bookingId) {
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        BaseUtils.validateObject(bookingId, "Booking id", false);
    }

    private void validateOwnerHotelAndCode(String ownerHotelId, String code) {
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        BaseUtils.validateObject(code, "Booking code", false);
    }

    private void validateOwnerHotelAndBookingIds(String ownerHotelId, List<String> bookingIds) {
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
    }

    private void validBookingsSize(List<Booking> bookings, List<String> bookingIds) {
        if (bookings.size() != bookingIds.size()) {
            log.warn("Have one or more booking not found in list id: {}", bookingIds);
            throw new BusinessException("Booking not found with list id: " + bookingIds);
        }
    }

    private Booking getBookingByCodeAndOwnerHotelId(String ownerHotelId, String code) {
        validateOwnerHotelAndCode(ownerHotelId, code);
        Optional<Booking> opBooking = bookingRepo.getBookingByCodeAndOwnerHotelIdAndIsDeleted(code, ownerHotelId, false);
        if (opBooking.isEmpty()) {
            log.warn("Booking not found with code:  {}", code);
            throw new BusinessException("Booking not found with code: " + code);
        }
        return opBooking.get();
    }
}