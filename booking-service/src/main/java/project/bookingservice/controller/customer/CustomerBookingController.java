package project.bookingservice.controller.customer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.bookingservice.service.CustomerBookingService;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.SearchBookingDto;
import project.commondto.dto.uri.BookingClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;

import java.util.List;

import static project.commonutils.BaseUtils.getClientIp;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.BOOKINGS + CommonUri.CUSTOMER)
public class CustomerBookingController {

    private final CustomerBookingService customerBookingService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_WRITE_BOOKING + "')")
    public ResponseEntity<?> createBooking(@AuthenticationPrincipal String userId,
                                           @Valid @RequestBody CreateBookingDto createBookingDto) {
        return BaseUtils.dataResponse("Create booking success",
                customerBookingService.createBooking(userId, createBookingDto),
                HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_READ_BOOKING + "')")
    public ResponseEntity<?> getBookings(SearchBookingDto searchBookingDto,
                                         Pageable pageable,
                                         @AuthenticationPrincipal String userId,
                                         String ownerHotelId) {
        return BaseUtils.dataResponse("Get bookings success",
                customerBookingService.getBookings(searchBookingDto,
                        pageable,
                        userId,
                        ownerHotelId), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_READ_BOOKING + "')")
    public ResponseEntity<?> getBooking(@AuthenticationPrincipal String userId,
                                        @PathVariable String bookingId) {
        return BaseUtils.dataResponse("Get booking success",
                customerBookingService.getBooking(userId, bookingId),
                HttpStatus.OK);
    }

    @GetMapping(BookingClientUri.GET_BOOKING_DETAILS)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_READ_BOOKING + "')")
    public ResponseEntity<?> getBookingDetails(@AuthenticationPrincipal String userId,
                                               String bookingId,
                                               Pageable pageable) {
        return BaseUtils.dataResponse("Get booking details success",
                customerBookingService.getBookingDetails(userId, bookingId, pageable),
                HttpStatus.OK);
    }

    @GetMapping(BookingClientUri.GET_INVENTORIES)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_READ_BOOKING + "')")
    public ResponseEntity<?> getBookingInventories(@AuthenticationPrincipal String userId,
                                                   String bookingDetailId,
                                                   Pageable pageable) {
        return BaseUtils.dataResponse("Get booking inventories success",
                customerBookingService.getBookingInventories(userId, bookingDetailId, pageable),
                HttpStatus.OK);
    }

    @PostMapping(BookingClientUri.CANCEL_BOOKING)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_UPDATE_BOOKING + "')")
    public ResponseEntity<?> cancelBooking(@AuthenticationPrincipal String userId,
                                           @RequestBody List<String> bookingIds) {
        return BaseUtils.dataResponse("Cancel booking success",
                customerBookingService.cancelBooking(userId, bookingIds),
                HttpStatus.OK);
    }
}
