package project.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PAYMENTS + CommonUri.OWNER)
@Slf4j
public class PaymentOwnerController {

    private final PaymentService paymentService;

    @PostMapping("/refund")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_PAYMENT + "')")
    public ResponseEntity<?> refundPayment(@RequestBody List<String> paymentIds){
        paymentService.processRefundPayment(paymentIds);
        return BaseUtils.baseResponse("Refund processing!", HttpStatus.OK);
    }

    @GetMapping("/paid-cash/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.OWNER_UPDATE_PAYMENT + "')")
    public ResponseEntity<?> updateCashPaid(@PathVariable String id){
        paymentService.updatePaidCashPayment(id);
        return BaseUtils.baseResponse("Update payment success", HttpStatus.OK);
    }
}
