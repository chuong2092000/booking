package project.bookingservice.service;

import project.commondto.dto.booking.BookingDto;
import project.commondto.dto.booking.BookingInventoryDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;

import java.util.List;

public interface InternalService {

    List<BookingDto> cancelBooking(List<String> bookingIds);

    List<BookingDto> failBooking(List<String> bookingIds);

    List<BookingInventoryDto> lockBookingInventories(List<String> bookingIds);

    List<BookingInventoryDto> releaseBookingInventories(List<String> bookingIds);

    List<BookingDto> getBookingsHaveBookingInventoriesLockedByBookingIds(List<String> bookingIds);

    void cleanBookingPaymentInfo(String bookingId);
}