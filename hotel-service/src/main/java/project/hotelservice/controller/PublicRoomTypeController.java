package project.hotelservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.roomtype.PublicRoomTypeService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.HOTELS + CommonUri.ROOM_TYPES)
public class PublicRoomTypeController {

    private final PublicRoomTypeService publicRoomTypeService;

    @GetMapping
    public ResponseEntity<?> getRoomTypes(@Valid @ModelAttribute SearchRoomTypesDto searchRoomTypesDto, String hotelId) {
        return BaseUtils.dataResponse("Get room-types success",
                publicRoomTypeService.getRoomTypes(searchRoomTypesDto, hotelId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomType(@PathVariable String id) {
        return BaseUtils.dataResponse("Get room-type success",
                publicRoomTypeService.getRoomType(id), HttpStatus.OK);
    }
}
