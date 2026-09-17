package project.hotelservice.service.roomtype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.admin.AdminFullRoomTypeDto;
import project.commondto.dto.hotel.admin.AdminRoomTypeDto;
import project.hotelservice.service.IRoomTypeService;

public interface AdminRoomTypeService extends IRoomTypeService {
    Page<AdminRoomTypeDto> getRoomTypes(String name,
                                        Pageable pageable,
                                        String hotelId, boolean isApproval);

    AdminFullRoomTypeDto getRoomType(String roomTypeId);

    AdminRoomTypeDto approveRoomType(String roomTypeId);

    void deleteRoomType(String roomTypeId);
}