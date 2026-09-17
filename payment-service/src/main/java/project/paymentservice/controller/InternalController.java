package project.paymentservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.commondto.dto.UpdateOutboxDto;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.uri.PaymentClientUri;
import project.commonutils.controller.OutboxControllerCM;
import project.paymentservice.entity.Outbox;
import project.paymentservice.service.PaymentService;
import project.paymentservice.service.impl.OutboxService;

@RestController
@RequestMapping(CommonUri.VERSION + CommonUri.INTERNAL + CommonUri.PAYMENTS)
@Slf4j
public class InternalController extends OutboxControllerCM<Outbox> {
    private PaymentService paymentService;

    public InternalController(@Autowired OutboxService outboxService, @Autowired PaymentService paymentService) {
        super(outboxService);
        this.paymentService = paymentService;
    }

    @GetMapping(PaymentClientUri.GET_OUTBOXES + "/{batchSize}")
    public ResponseEntity<?> getOutBoxes(@PathVariable Integer batchSize) {
        return super.getOutBoxes(batchSize);
    }

    @PatchMapping(PaymentClientUri.UPDATE_STATUS_OUTBOXES)
    public ResponseEntity<?> updateSentOutbox(@RequestBody UpdateOutboxDto request) {
        return super.updateStatusOutboxes(request);
    }

}