package project.bookingservice.mapper;

import org.mapstruct.Mapper;
import project.commondto.dto.booking.AdminBookingDto;
import project.commondto.dto.booking.BookingDto;
import project.bookingservice.entity.Booking;
import project.commondto.dto.booking.BookingResponse;
import project.commondto.dto.booking.OwnerBookingDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.commonutils.config.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface BookingMapper {
    BookingDto toDto(Booking booking);

    Booking toEntity(BookingDto bookingDto);

    BookingDto toDto(BookingResponse bookingResponse);

    AdminBookingDto toAdminDto(Booking booking);

    OwnerBookingDto toOwnerDto(Booking booking);

    BookingPaymentDto toPaymentDto(Booking booking);
}
