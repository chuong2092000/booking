package project.bookingservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.booking.AdminBookingDto;
import project.commondto.dto.booking.BookingDetailDto;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.booking.SearchBookingDto;

import java.util.List;

public interface AdminBookingService extends IBookingService {

    void deleteBooking(String bookingId);

    AdminBookingDto getBooking(String bookingId);

    Page<AdminBookingDto> getBookings(SearchBookingDto searchBookingDto,
                                      Pageable pageable,
                                      String userId,
                                      String ownerHotelId);

    Page<BookingDetailDto> getBookingDetails(String bookingId, Pageable pageable);
}