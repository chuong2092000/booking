package project.hotelservice.service.roomtype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.FullRoomTypeDto;
import project.commondto.dto.hotel.dto.SearchRoomTypeRes;
import project.hotelservice.service.IRoomTypeService;

import java.util.List;

public interface PublicRoomTypeService extends IRoomTypeService {
    List<SearchRoomTypeRes> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, String hotelId);

    FullRoomTypeDto getRoomType(String roomTypeId);
}