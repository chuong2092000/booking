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
import project.bookingservice.service.AdminBookingService;
import project.bookingservice.service.core.CoreBookingService;
import project.commondto.dto.booking.AdminBookingDto;
import project.commondto.dto.booking.BookingDetailDto;
import project.commondto.dto.booking.SearchBookingDto;
import project.commonutils.BaseUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepo bookingRepo;

    private final BookingMapper bookingMapper;

    private final CoreBookingService coreBookingService;

    private final OrchestratorService orchestratorService;

    private final BookingDetailMapper bookingDetailMapper;

    @Override
    public void deleteBooking(String bookingId) {
        BaseUtils.validateObject(bookingId, "Booking id", false);
        coreBookingService.withBookingLocks(List.of(bookingId), () -> {
            orchestratorService.deleteBooking(bookingId);
            return null;
        });
    }

    @Transactional(readOnly = true)
    @Override
    public AdminBookingDto getBooking(String bookingId) {

        BaseUtils.validateObject(bookingId, "Booking id", false);
        Optional<Booking> opBooking = bookingRepo.getBookingByIdAndIsDeleted(bookingId, false);

        return bookingMapper.toAdminDto(
                coreBookingService.getBookingFromOption(bookingId, opBooking));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookingDetailDto> getBookingDetails(String bookingId, Pageable pageable) {
        BaseUtils.validateObject(bookingId, "Booking id", false);
        return coreBookingService.searchBookingDetails(bookingId, pageable).map(bookingDetailMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdminBookingDto> getBookings(SearchBookingDto searchBookingDto, Pageable pageable, String userId, String ownerHotelId) {
        return coreBookingService.searchBookings(searchBookingDto, pageable, userId, ownerHotelId)
                .map(bookingMapper::toAdminDto);
    }
}