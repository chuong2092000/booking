package project.bookingservice.mapper;

import org.mapstruct.Mapper;
import project.bookingservice.entity.BookingInventory;
import project.commondto.dto.booking.BookingInventoryDto;
import project.commonutils.config.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface BookingInventoryMapper {
    BookingInventoryDto toDto(BookingInventory bookingInventory);

    BookingInventory toEntity(BookingInventoryDto bookingInventoryDto);
}
