
package project.bookingservice.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.bookingservice.service.AdminBookingService;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.booking.SearchBookingDto;
import project.commondto.dto.uri.BookingClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.BOOKINGS + CommonUri.ADMIN)
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_BOOKING + "', '" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getBookings(SearchBookingDto searchBookingDto,
                                         Pageable pageable,
                                         String ownerHotelId,
                                         String userId) {

        return BaseUtils.dataResponse("Get bookings success",
                adminBookingService.getBookings(searchBookingDto, pageable, userId, ownerHotelId),
                HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_BOOKING + "', '" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getBooking(@PathVariable String bookingId) {
        return BaseUtils.dataResponse("Get booking success",
                adminBookingService.getBooking(bookingId),
                HttpStatus.OK);
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_DELETE_BOOKING + "' + '" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingId) {
        adminBookingService.deleteBooking(bookingId);
        return BaseUtils.baseResponse("Delete booking success", HttpStatus.NO_CONTENT);
    }

    @GetMapping(BookingClientUri.GET_BOOKING_DETAILS)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_READ_BOOKING + "', '" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> getBookingDetails(String bookingId, Pageable pageable) {
        return BaseUtils.dataResponse("Get booking details success",
                adminBookingService.getBookingDetails(bookingId, pageable),
                HttpStatus.OK);
    }
}
