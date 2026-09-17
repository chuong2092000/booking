package project.hotelservice.controller.internal;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.commondto.dto.UpdateOutboxDto;
import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.ReservedRequest;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.uri.HotelClientUri;
import project.commonutils.BaseUtils;
import project.commonutils.controller.OutboxControllerCM;
import project.hotelservice.entity.Outbox;
import project.hotelservice.service.InternalService;
import project.hotelservice.service.impl.OutboxService;

import java.util.List;

@RestController
@RequestMapping(CommonUri.VERSION + CommonUri.INTERNAL + CommonUri.HOTELS)
public class InternalController extends OutboxControllerCM<Outbox> {
    private final InternalService internalService;

    public InternalController(@Autowired OutboxService outboxService, @Autowired InternalService internalService) {
        super(outboxService);
        this.internalService = internalService;
    }

    @GetMapping(HotelClientUri.GET_OUTBOXES + "/{batchSize}")
    public ResponseEntity<?> getOutBoxes(@PathVariable Integer batchSize) {
        return super.getOutBoxes(batchSize);
    }

    @PatchMapping(HotelClientUri.UPDATE_STATUS_OUTBOXES)
    public ResponseEntity<?> updateSentOutbox(@RequestBody UpdateOutboxDto request) {
        return super.updateStatusOutboxes(request);
    }

    @PostMapping(HotelClientUri.GET_INTERNAL_ROOM_TYPE)
    public ResponseEntity<?> getFullHotelByIds(@RequestBody CreateBookingDto createBookingDto) {
        return BaseUtils.dataResponse("Get room-types success",
                internalService.getRoomTypeByIds(createBookingDto),
                HttpStatus.OK);
    }
//
//    @PostMapping(HotelClientUri.INR)
//    public ResponseEntity<?> increaseQuantity(@RequestBody List<ResultBookingsDto> resultBookingsDtos) {
//        internalService.increaseQuantity(resultBookingsDtos);
//        return BaseUtils.baseResponse("success", HttpStatus.OK);
//    }
//
//    @PostMapping(HotelClientUri.DER)
//    public ResponseEntity<?> decreaseQuantity(@RequestBody List<ResultBookingsDto> resultBookingsDtos) {
//        internalService.decreaseQuantity(resultBookingsDtos);
//        return BaseUtils.baseResponse("success", HttpStatus.OK);
//    }
}
