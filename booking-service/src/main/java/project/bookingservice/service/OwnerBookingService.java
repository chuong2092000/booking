package project.bookingservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.booking.BookingDetailDto;
import project.commondto.dto.booking.OwnerBookingDto;
import project.commondto.dto.booking.SearchBookingDto;

import java.util.List;

public interface OwnerBookingService extends IBookingService {
    List<OwnerBookingDto> cancelBooking(String ownerHotelId, List<String> bookingIds);

    OwnerBookingDto getBooking(String ownerHotelId, String bookingId);

    Page<OwnerBookingDto> getBookings(SearchBookingDto searchBookingDto,
                                      Pageable pageable,
                                      String userId,
                                      String ownerHotelId);

    Page<BookingDetailDto> getBookingDetails(String ownerHotelId, String bookingId, Pageable pageable);

    void checkInBooking(String ownerHotelId, String code);

    void checkOutBooking(String ownerHotelId, String code);
}