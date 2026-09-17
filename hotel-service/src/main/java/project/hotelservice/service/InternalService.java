package project.hotelservice.service;

import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commondto.dto.hotel.internal.InternalBookingRoomDto;

import java.util.List;

public interface InternalService {
    List<InternalBookingRoomDto> getRoomTypeByIds(CreateBookingDto createBookingDto);

    void decreaseQuantity(List<String> bookingIds, List<BookingRoomInvDto> bookingRoomInvDtos);

    void increaseQuantity(List<String> bookingIds, List<BookingRoomInvDto> bookingRoomInvDtos);
}
