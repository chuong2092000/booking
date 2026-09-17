package project.hotelservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.commondto.dto.hotel.admin.AdminFullRoomTypeDto;
import project.commondto.dto.hotel.admin.AdminRoomTypeDto;
import project.commondto.dto.hotel.dto.FullRoomTypeDto;
import project.commondto.dto.hotel.dto.RoomTypeDto;
import project.commondto.dto.hotel.dto.SearchRoomTypeRes;
import project.commondto.dto.hotel.internal.InternalRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerFullRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeDto;
import project.commonutils.config.CentralMapperConfig;
import project.hotelservice.entity.RoomType;

@Mapper(config = CentralMapperConfig.class)
public interface RoomTypeMapper {
    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    RoomTypeDto toDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isApproval", expression = "java(roomType.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    AdminRoomTypeDto toAdminDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isApproval", expression = "java(roomType.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    OwnerRoomTypeDto toOwnerDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    FullRoomTypeDto toFullDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isApproval", expression = "java(roomType.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    OwnerFullRoomTypeDto toOwnerFullDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isApproval", expression = "java(roomType.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    AdminFullRoomTypeDto toAdminFullDto(RoomType roomType);

    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    InternalRoomTypeDto toInternalDto(RoomType roomType);

    RoomType toEntity(RoomTypeDto roomTypeDto);

    RoomType toAdminEntity(AdminRoomTypeDto adminRoomTypeDto);

    RoomType toOwnerEntity(OwnerRoomTypeDto ownerRoomTypeDto);
    @Mapping(target = "urlImage", expression = "java(roomType.getImageUri() + \"/\" + roomType.getImageName())")
    @Mapping(target = "isProcessedIm", expression = "java(roomType.isProcessedIm())")
    SearchRoomTypeRes toSearchRoomTypeRes(RoomType roomType);
}
