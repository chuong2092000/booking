package project.bookingservice.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.bookingservice.entity.Outbox;
import project.bookingservice.service.InternalService;
import project.bookingservice.service.impl.OutboxService;
import project.commondto.dto.UpdateOutboxDto;
import project.commondto.dto.uri.BookingClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.commonutils.controller.OutboxControllerCM;

import java.util.List;

@RestController
@RequestMapping(CommonUri.VERSION + CommonUri.INTERNAL + CommonUri.BOOKINGS)
public class InternalController extends OutboxControllerCM<Outbox> {

    private final InternalService internalService;


    public InternalController(@Autowired OutboxService outboxService, @Autowired InternalService internalService) {
        super(outboxService);
        this.internalService = internalService;
    }

    @GetMapping(BookingClientUri.GET_OUTBOXES + "/{batchSize}")
    public ResponseEntity<?> getOutBoxes(@PathVariable Integer batchSize) {
        return super.getOutBoxes(batchSize);
    }

    @PatchMapping(BookingClientUri.UPDATE_STATUS_OUTBOXES)
    public ResponseEntity<?> updateSentOutbox(@RequestBody UpdateOutboxDto request) {
        return super.updateStatusOutboxes(request);
    }

    @PostMapping("/ids")
    public ResponseEntity<?> getBookingsHaveBookingInventoriesLockedByBookingIds(@RequestBody List<String> bookingIds) {
        return BaseUtils.dataResponse("Get bookings success",
                internalService.getBookingsHaveBookingInventoriesLockedByBookingIds(bookingIds), HttpStatus.OK);
    }

}
