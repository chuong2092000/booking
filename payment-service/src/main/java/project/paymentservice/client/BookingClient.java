package project.paymentservice.client;

import project.commondto.dto.booking.BookingDto;

import java.util.List;

public interface BookingClient {
    List<BookingDto> getBookingsHaveBookingInventoriesLockedByBookingIds(List<String> bookingIds);
}
