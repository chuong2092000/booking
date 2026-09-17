package project.hotelservice.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.admin.AdminFullRoomTypeDto;
import project.commondto.dto.hotel.admin.AdminRoomTypeDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.roomtype.AdminRoomTypeService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.HOTELS + CommonUri.ROOM_TYPES + CommonUri.ADMIN)
public class AdminRoomTypeController {

    private final AdminRoomTypeService adminRoomTypeService;


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_DELETE_ROOM_TYPE + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> deleteRoomType(@PathVariable String id) {
        adminRoomTypeService.deleteRoomType(id);
        return BaseUtils.baseResponse("Delete room-type success", HttpStatus.OK);
    }

    @PatchMapping("/approval/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_APPROVE_ROOM_TYPE + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> approveRoomType(@PathVariable String id) {
        AdminRoomTypeDto roomType = adminRoomTypeService.approveRoomType(id);
        return BaseUtils.dataResponse("Approve room-type success", roomType, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_ROOM_TYPE + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getRoomTypes(String hotelId, String name, Pageable pageable, boolean isApproval) {
        Page<AdminRoomTypeDto> roomTypes = adminRoomTypeService.getRoomTypes(name, pageable, hotelId, isApproval);
        return BaseUtils.dataResponse("Get room-types success", roomTypes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_ROOM_TYPE + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getRoomType(@PathVariable String id) {
        AdminFullRoomTypeDto roomType = adminRoomTypeService.getRoomType(id);
        return BaseUtils.dataResponse("Get room-type success", roomType, HttpStatus.OK);
    }
}
