package project.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.payment.vnpay.IpnRequest;
import project.commondto.dto.payment.vnpay.IpnResponse;
import project.commondto.dto.uri.CommonUri;
import project.paymentservice.processor.ProcessorService;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.PAYMENTS)
@Slf4j
public class PaymentPublicController {

    private final ProcessorService processorService;

    @GetMapping("/vn-pay/ipn")
    public ResponseEntity<?> ipnPayment(IpnRequest ipnRequest) {
        IpnResponse response;
        try {
            response = processorService.processIpnPayment(ipnRequest);
        } catch (Exception e) {
            log.error("[VNPAY IPN] Unexpected error, txnRef={}", ipnRequest.getVnp_TxnRef(), e);
            response = IpnResponse.unknownError();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/success")
    public ResponseEntity<?> test() {
        return new ResponseEntity<>("Payment success", HttpStatus.OK);
    }
}
