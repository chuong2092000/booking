package project.bookingservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.booking.*;

import java.util.List;

public interface CustomerBookingService extends IBookingService {

    BookingDto createBooking(String userId, CreateBookingDto createBookingDto);

    List<BookingDto> cancelBooking(String userId, List<String> bookingIds);

    BookingDto getBooking(String userId, String bookingId);

    Page<BookingDto> getBookings(SearchBookingDto searchBookingDto,
                                 Pageable pageable,
                                 String userId,
                                 String ownerHotelId);

    Page<BookingDetailDto> getBookingDetails(String userId, String bookingId, Pageable pageable);

    Page<BookingInventoryDto> getBookingInventories(String userId, String bookingDetailId, Pageable pageable);
}