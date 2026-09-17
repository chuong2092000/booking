package project.commonutils;

import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBusiness {

    public static List<BookingRoomInvDto> detachRoomInventoriesFromResultBooking(List<ResultBookingsDto> resultBookings) {
        BaseUtils.validateListObject(resultBookings, "Result Bookings", false);

        List<BookingRoomInvDto> bookingRoomInvDto = new ArrayList<>();

        resultBookings.forEach(rs -> {
            BaseUtils.validateObject(rs, "Result booking", false);
            BaseUtils.validateObject(rs.getBookingId(), "Booking id", false);
            rs.getInventories().forEach(rt -> {
                BaseUtils.validateObject(rt.getId(), "Room inv id", false);
                BaseUtils.validateObject(rt.getQuantity(), "Room inv quantity", false);
                bookingRoomInvDto.add(rt);
            });
        });

        return bookingRoomInvDto;
    }

    public static List<String> detachBookingIdsFromResultBooking(List<ResultBookingsDto> resultBookings) {
        BaseUtils.validateListObject(resultBookings, "Result Bookings", false);

        List<String> bookingIds = new ArrayList<>();

        resultBookings.forEach(rs -> {
            BaseUtils.validateObject(rs, "Result booking", false);
            BaseUtils.validateObject(rs.getBookingId(), "Booking id", false);
            bookingIds.add(rs.getBookingId());
        });

        return bookingIds;
    }
}
