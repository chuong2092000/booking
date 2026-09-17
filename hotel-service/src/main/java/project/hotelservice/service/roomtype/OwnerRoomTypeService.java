package project.hotelservice.service.roomtype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.request.CreateRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerFullRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeDto;
import project.hotelservice.service.IRoomTypeService;

public interface OwnerRoomTypeService extends IRoomTypeService {
    OwnerRoomTypeDto createRoomType(String ownerHotelId, String hotelId, CreateRoomTypeDto createRoomTypeDto, MultipartFile mainImageFile, MultipartFile[] supportImageFile);

    Page<OwnerRoomTypeDto> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto,
                                        Pageable pageable,
                                        String hotelId,
                                        String ownerHotelId, boolean isApproval);

    OwnerFullRoomTypeDto getRoomType(String ownerHotelId, String roomTypeId);
}