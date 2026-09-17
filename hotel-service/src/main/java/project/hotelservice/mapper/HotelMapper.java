package project.hotelservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.commondto.dto.hotel.admin.AdminFullHotelDto;
import project.commondto.dto.hotel.admin.AdminHotelDto;
import project.commondto.dto.hotel.dto.FullHotelDto;
import project.commondto.dto.hotel.dto.HotelDto;
import project.commondto.dto.hotel.internal.InternalHotelDto;
import project.commondto.dto.hotel.owner.OwnerFullHotelDto;
import project.commondto.dto.hotel.owner.OwnerHotelDto;
import project.commonutils.config.CentralMapperConfig;
import project.hotelservice.entity.Hotel;
import project.hotelservice.utils.JsonHelper;

@Mapper(config = CentralMapperConfig.class, uses = {JsonHelper.class})
public interface HotelMapper {

    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    HotelDto toDto(Hotel hotel);

    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isApproval", expression = "java(hotel.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    AdminHotelDto toAdminDto(Hotel hotel);

    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isApproval", expression = "java(hotel.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    OwnerHotelDto toOwnerDto(Hotel hotel);

    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    FullHotelDto toFullDto(Hotel hotel);
    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isApproval", expression = "java(hotel.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    AdminFullHotelDto toAdminFullDto(Hotel hotel);
    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    @Mapping(target = "isApproval", expression = "java(hotel.isApproval())")
    @Mapping(target = "isProcessedIm", expression = "java(hotel.isProcessedIm())")
    OwnerFullHotelDto toOwnerFullDto(Hotel hotel);

    @Mapping(target = "urlImage", expression = "java(hotel.getImageUri() + \"/\" + hotel.getImageName())")
    InternalHotelDto toInternalDto(Hotel hotel);

    Hotel toEntity(HotelDto hotelDto);
}
