package project.bookingservice.controller.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.bookingservice.service.OwnerBookingService;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.booking.SearchBookingDto;
import project.commondto.dto.uri.BookingClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.BOOKINGS + CommonUri.OWNER)
public class OwnerBookingController {

    private final OwnerBookingService ownerBookingService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_BOOKING + "')")
    public ResponseEntity<?> getBookings(@AuthenticationPrincipal String ownerHotelId,
                                         SearchBookingDto searchBookingDto,
                                         Pageable pageable,
                                         String userId) {
        return BaseUtils.dataResponse("Get bookings success",
                ownerBookingService.getBookings(searchBookingDto, pageable, userId, ownerHotelId),
                HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_BOOKING + "')")
    public ResponseEntity<?> getBooking(@AuthenticationPrincipal String ownerHotelId,
                                        @PathVariable String bookingId) {
        return BaseUtils.dataResponse("Get booking success",
                ownerBookingService.getBooking(ownerHotelId, bookingId),
                HttpStatus.OK);
    }

    @GetMapping("/check-in/{code}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_BOOKING + "')")
    public ResponseEntity<?> checkInBooking(@AuthenticationPrincipal String ownerHotelId, @PathVariable String code) {
        ownerBookingService.checkInBooking(ownerHotelId, code);
        return BaseUtils.baseResponse("Check-in success", HttpStatus.OK);
    }

    @GetMapping("/check-out/{code}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_BOOKING + "')")
    public ResponseEntity<?> checkOutBooking(@AuthenticationPrincipal String ownerHotelId, @PathVariable String code) {
        ownerBookingService.checkOutBooking(ownerHotelId, code);
        return BaseUtils.baseResponse("Check-out success", HttpStatus.OK);
    }

//    @PostMapping(BookingClientUri.CONFIRM_BOOKING)
//    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_BOOKING + "')")
//    public ResponseEntity<?> confirmBooking(@AuthenticationPrincipal String ownerHotelId,
//                                            @RequestBody List<String> bookingIds) {
//        return BaseUtils.dataResponse("Confirm booking success",
//                ownerBookingService.confirmBooking(ownerHotelId, bookingIds),
//                HttpStatus.OK);
//    }

    @PostMapping(BookingClientUri.CANCEL_BOOKING)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_BOOKING + "')")
    public ResponseEntity<?> cancelBooking(@AuthenticationPrincipal String ownerHotelId,
                                           @RequestBody List<String> bookingIds) {
        return BaseUtils.dataResponse("Cancel booking success",
                ownerBookingService.cancelBooking(ownerHotelId, bookingIds),
                HttpStatus.OK);
    }

    @GetMapping(BookingClientUri.GET_BOOKING_DETAILS)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_READ_BOOKING + "')")
    public ResponseEntity<?> getBookingDetails(@AuthenticationPrincipal String ownerHotelId,
                                               String bookingId, Pageable pageable) {
        return BaseUtils.dataResponse("Get booking details success",
                ownerBookingService.getBookingDetails(ownerHotelId, bookingId, pageable),
                HttpStatus.OK);
    }
}
