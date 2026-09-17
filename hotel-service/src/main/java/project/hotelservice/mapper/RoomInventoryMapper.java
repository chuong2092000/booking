package project.hotelservice.mapper;

import org.mapstruct.Mapper;
import project.commondto.dto.hotel.admin.AdminFullRoomInventoryDto;
import project.commondto.dto.hotel.admin.AdminRoomInventoryDto;
import project.commondto.dto.hotel.internal.InternalRoomInventoryDto;
import project.commondto.dto.hotel.owner.OwnerFullRoomInventoryDto;
import project.commondto.dto.hotel.owner.OwnerRoomInventoryDto;
import project.commondto.dto.hotel.dto.RoomInventoryDto;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.utils.JsonHelper;

@Mapper(componentModel = "spring", uses = {JsonHelper.class})
public interface RoomInventoryMapper {
    RoomInventoryDto toDto(RoomInventory roomInventory);

    AdminRoomInventoryDto toAdminDto(RoomInventory roomInventory);

    AdminFullRoomInventoryDto toAdminFullDto(RoomInventory roomInventory);

    OwnerRoomInventoryDto toOwnerDto(RoomInventory roomInventory);
    OwnerFullRoomInventoryDto toOwnerFullDto(RoomInventory roomInventory);

    InternalRoomInventoryDto toInternalDto(RoomInventory roomInventory);

    RoomInventory toEntity(RoomInventoryDto roomInventoryDto);

    RoomInventory toAdminEntity(AdminRoomInventoryDto roomInventoryDto);

    RoomInventory toOwnerEntity(OwnerRoomInventoryDto roomInventoryDto);

    RoomInventoryDto toDto(InternalRoomInventoryDto internalRoomInventoryDto);
    InternalRoomInventoryDto toInternalDto(RoomInventoryDto roomInventoryDto);
}
