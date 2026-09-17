package project.hotelservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.commondto.dto.hotel.admin.AdminHotelImageDto;
import project.commondto.dto.hotel.dto.HotelImageDto;
import project.commondto.dto.hotel.owner.OwnerHotelImageDto;
import project.commonutils.config.CentralMapperConfig;
import project.hotelservice.entity.HotelImage;
import project.hotelservice.utils.JsonHelper;

@Mapper(config = CentralMapperConfig.class,uses = {JsonHelper.class})
public interface HotelImageMapper {
    @Mapping(target = "urlImage", expression = "java(hotelImage.getUri() + \"/\" + hotelImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(hotelImage.isProcessed())")
    HotelImageDto toDto(HotelImage hotelImage);
    @Mapping(target = "urlImage", expression = "java(hotelImage.getUri() + \"/\" + hotelImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(hotelImage.isProcessed())")
    AdminHotelImageDto toAdminDto(HotelImage hotelImage);
    @Mapping(target = "urlImage", expression = "java(hotelImage.getUri() + \"/\" + hotelImage.getName())")
    @Mapping(target = "isProcessed", expression = "java(hotelImage.isProcessed())")
    OwnerHotelImageDto toOwnerDto(HotelImage hotelImage);
}
