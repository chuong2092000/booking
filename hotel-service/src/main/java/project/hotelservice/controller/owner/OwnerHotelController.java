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
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.dto.request.CreateHotelDto;
import project.commondto.dto.hotel.owner.OwnerFullHotelDto;
import project.commondto.dto.hotel.owner.OwnerHotelDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.hotel.OwnerHotelService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.HOTELS + CommonUri.OWNER)
public class OwnerHotelController {

    private final OwnerHotelService ownerHotelService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_WRITE_HOTEL + "')")
    public ResponseEntity<?> createHotel(
            @AuthenticationPrincipal String ownerHotelId,
            @Valid @ModelAttribute CreateHotelDto createHotelDto,
            @RequestParam("mainImageFile") MultipartFile mainImageFile,
            @RequestParam(value = "supportImageFiles") MultipartFile[] supportImageFiles
    ) {
        OwnerHotelDto hotel = ownerHotelService.createHotel(createHotelDto, ownerHotelId, mainImageFile, supportImageFiles);
        return BaseUtils.dataResponse("Create hotel success", hotel, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_HOTEL + "')")
    public ResponseEntity<?> getHotels(@AuthenticationPrincipal String ownerHotelId,@Valid @ModelAttribute SearchHotelDto searchHotelDto, Pageable pageable, boolean isApproval) {
        Page<OwnerHotelDto> hotels = ownerHotelService.getHotels(searchHotelDto, pageable, ownerHotelId, isApproval);
        return BaseUtils.dataResponse("Get hotels success", hotels, HttpStatus.OK);
    }


    @GetMapping("/{hotelId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_HOTEL + "')")
    public ResponseEntity<?> getHotel(@AuthenticationPrincipal String ownerHotelId, @PathVariable String hotelId) {
        OwnerFullHotelDto hotel = ownerHotelService.getHotel(ownerHotelId, hotelId);
        return BaseUtils.dataResponse("Get hotel success", hotel, HttpStatus.OK);
    }
}