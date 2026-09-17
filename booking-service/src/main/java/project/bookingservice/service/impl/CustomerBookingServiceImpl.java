package project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.bookingservice.entity.Booking;
import project.bookingservice.entity.BookingDetail;
import project.bookingservice.mapper.BookingDetailMapper;
import project.bookingservice.mapper.BookingInventoryMapper;
import project.bookingservice.mapper.BookingMapper;
import project.bookingservice.orchestrator.OrchestratorService;
import project.bookingservice.repo.BookingDetailRepo;
import project.bookingservice.repo.BookingRepo;
import project.bookingservice.service.CustomerBookingService;
import project.bookingservice.service.core.CoreBookingService;
import project.commondto.dto.booking.*;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerBookingServiceImpl implements CustomerBookingService {

    private final BookingRepo bookingRepo;

    private final BookingMapper bookingMapper;

    private final CoreBookingService coreBookingService;

    private final BookingDetailMapper bookingDetailMapper;

    private final BookingDetailRepo bookingDetailRepo;

    private final BookingInventoryMapper bookingInventoryMapper;

    private final OrchestratorService orchestratorService;

    @Transactional
    @Override
    public BookingDto createBooking(String userId, CreateBookingDto createBookingDto) {

        BaseUtils.validateObject(userId, "User id", false);
        BaseUtils.validateObject(createBookingDto, "Create booking DTO", false);

        return bookingMapper.toDto(coreBookingService.processBookings(userId, createBookingDto));
    }

    @Override
    public List<BookingDto> cancelBooking(String userId, List<String> bookingIds) {

        validateUserAndBookingIds(userId, bookingIds);

        List<Booking> bookings = bookingRepo.getBookingsByIdAndUserIdAndIsDeleted(bookingIds, userId, false);

        validBookingsSize(bookings, bookingIds);

        return coreBookingService.withBookingLocks(bookingIds, () ->
                orchestratorService.cancelBookings(bookings).stream().map(bookingMapper::toDto).toList());
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBooking(String userId, String bookingId) {

        validateUserAndBookingId(userId, bookingId);

        Optional<Booking> opBooking = bookingRepo.getBookingByIdAndKeycloakIdAndIsDeleted(bookingId, userId, false);

        return bookingMapper.toDto(
                coreBookingService.getBookingFromOption(bookingId, opBooking));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookingDetailDto> getBookingDetails(String userId, String bookingId, Pageable pageable) {

        coreBookingService.existsBookingByBookingIdAndUserId(bookingId, userId);

        return coreBookingService.searchBookingDetails(bookingId, pageable)
                .map(bookingDetailMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookingInventoryDto> getBookingInventories(String userId, String bookingDetailId, Pageable pageable) {

        BaseUtils.validateObject(bookingDetailId, "Booking detail id", false);

        Optional<BookingDetail> opBookingDetail = bookingDetailRepo.findBookingDetailByIdAndIsDeleted(bookingDetailId, false);
        BookingDetail bookingDetail = coreBookingService.getBookingDetailFromOption(bookingDetailId, opBookingDetail);

        coreBookingService.existsBookingByBookingIdAndUserId(bookingDetail.getBookingId(), userId);

        return coreBookingService.searchBookingInventories(bookingDetailId, pageable)
                .map(bookingInventoryMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookingDto> getBookings(SearchBookingDto searchBookingDto, Pageable pageable, String userId, String ownerHotelId) {
        BaseUtils.validateObject(userId, "User id", false);
        return coreBookingService.searchBookings(searchBookingDto, pageable, userId, ownerHotelId).map(bookingMapper::toDto);
    }

    private void validateUserAndBookingId(String userId, String bookingId) {
        BaseUtils.validateObject(userId, "User id", false);
        BaseUtils.validateObject(bookingId, "Booking id", false);
    }

    private void validateUserAndBookingIds(String userId, List<String> bookingIds) {
        BaseUtils.validateObject(userId, "User id", false);
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
    }

    private void validBookingsSize(List<Booking> bookings, List<String> bookingIds) {
        if (bookings.size() != bookingIds.size()) {
            log.warn("Have one or more booking not found in list id: {}", bookingIds);
            throw new BusinessException("Booking not found with list id: " + bookingIds);
        }
    }
}