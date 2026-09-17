package project.hotelservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.commondto.dto.hotel.admin.AdminRoomTypeImageDto;
import project.commondto.dto.hotel.dto.RoomTypeImageDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeImageDto;
import project.hotelservice.entity.RoomTypeImage;
import project.hotelservice.utils.JsonHelper;

@Mapper(componentModel = "spring", uses = {JsonHelper.class})
public interface RoomTypeImageMapper {
    @Mapping(target = "urlImage", expression = "java(roomTypeImage.getUri() + \"/\" + roomTypeImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(roomTypeImage.isProcessed())")
    RoomTypeImageDto toDto(RoomTypeImage roomTypeImage);

    @Mapping(target = "urlImage", expression = "java(roomTypeImage.getUri() + \"/\" + roomTypeImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(roomTypeImage.isProcessed())")
    AdminRoomTypeImageDto toAdminDto(RoomTypeImage roomTypeImage);

    @Mapping(target = "urlImage", expression = "java(roomTypeImage.getUri() + \"/\" + roomTypeImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(roomTypeImage.isProcessed())")
    OwnerRoomTypeImageDto toOwnerDto(RoomTypeImage roomTypeImage);
}
