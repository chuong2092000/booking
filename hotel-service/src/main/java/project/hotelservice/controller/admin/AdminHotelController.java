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
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.admin.AdminFullHotelDto;
import project.commondto.dto.hotel.admin.AdminHotelDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.hotel.AdminHotelService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.HOTELS + CommonUri.ADMIN)
public class AdminHotelController {

    private final AdminHotelService adminHotelService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_DELETE_HOTEL + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> deleteHotel(@PathVariable String id) {
        adminHotelService.deleteHotel(id);
        return BaseUtils.baseResponse("Delete hotel success", HttpStatus.OK);
    }

    @PatchMapping("/approval/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_APPROVE_HOTEL + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> approveHotel(@PathVariable String id) {
        AdminHotelDto adminHotelDto = adminHotelService.approveHotel(id);
        return BaseUtils.dataResponse("Approve hotel success", adminHotelDto, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_HOTEL + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getHotels(String ownerHotelId, @Valid @ModelAttribute SearchHotelDto searchHotelDto, Pageable pageable, boolean isApproval) {
        Page<AdminHotelDto> hotels = adminHotelService.getHotels(searchHotelDto, pageable, ownerHotelId, isApproval);
        return BaseUtils.dataResponse("Get hotels success", hotels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_HOTEL + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getHotel(@PathVariable String id) {
        AdminFullHotelDto hotel = adminHotelService.getHotel(id);
        return BaseUtils.dataResponse("Get hotel success", hotel, HttpStatus.OK);
    }

}
