package project.bookingservice.client;

import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.ReservedRequest;
import project.commondto.dto.hotel.internal.InternalBookingRoomDto;

import java.util.List;

public interface HotelClient {
    List<InternalBookingRoomDto> getRoomTypeByIds(CreateBookingDto createBookingDto);
}
