package project.hotelservice.controller.owner;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.request.CreateRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerFullRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.roomtype.OwnerRoomTypeService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.HOTELS + CommonUri.ROOM_TYPES + CommonUri.OWNER)
public class OwnerRoomTypeController {

    private final OwnerRoomTypeService ownerRoomTypeService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_WRITE_ROOM_TYPE + "')")
    public ResponseEntity<?> createRoomType(
            @AuthenticationPrincipal String ownerHotelId,
            @RequestParam("hotelId") String hotelId,
            @Valid @ModelAttribute CreateRoomTypeDto createRoomTypeDto,
            @RequestParam("mainImageFile") MultipartFile mainImageFile,
            @RequestParam(value = "supportImageFiles") MultipartFile[] supportImageFiles
    ) {
        OwnerRoomTypeDto roomType = ownerRoomTypeService.createRoomType(ownerHotelId, hotelId, createRoomTypeDto, mainImageFile, supportImageFiles);
        return BaseUtils.dataResponse("Create room-type success", roomType, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_ROOM_TYPE + "')")
    public ResponseEntity<?> getRoomTypes(@AuthenticationPrincipal String ownerHotelId, String hotelId, @Valid @ModelAttribute SearchRoomTypesDto searchRoomTypesDto, Pageable pageable, boolean isApproval) {
        Page<OwnerRoomTypeDto> roomTypes = ownerRoomTypeService.getRoomTypes(searchRoomTypesDto, pageable, hotelId, ownerHotelId, isApproval);
        return BaseUtils.dataResponse("Get room-types success", roomTypes, HttpStatus.OK);
    }

    @GetMapping("/{roomTypeId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_ROOM_TYPE + "')")
    public ResponseEntity<?> getRoomType(@AuthenticationPrincipal String ownerHotelId, @PathVariable String roomTypeId) {
        OwnerFullRoomTypeDto roomType = ownerRoomTypeService.getRoomType(ownerHotelId, roomTypeId);
        return BaseUtils.dataResponse("Get room-type success", roomType, HttpStatus.OK);
    }
}
