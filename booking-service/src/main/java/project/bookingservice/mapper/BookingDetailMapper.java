package project.bookingservice.mapper;

import org.mapstruct.Mapper;
import project.commondto.dto.booking.BookingDetailDto;
import project.bookingservice.entity.BookingDetail;
import project.commonutils.config.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface BookingDetailMapper {
    BookingDetailDto toDto(BookingDetail bookingDetail);

    BookingDetail toEntity(BookingDetailDto bookingDetailDto);
}
