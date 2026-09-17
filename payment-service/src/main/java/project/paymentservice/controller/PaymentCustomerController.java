package project.paymentservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.payment.CreatePaymentDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PAYMENTS + CommonUri.CUSTOMER)
@Slf4j
public class PaymentCustomerController {

    private final PaymentService paymentService;

    @PostMapping("/pay-online")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_WRITE_PAYMENT + "')")
    public ResponseEntity<?> createPaymentOnline(@Valid @RequestBody CreatePaymentDto createPaymentDto, HttpServletRequest request) {
        String clientIp = BaseUtils.getClientIp(request);
        String urlPayment = paymentService.createPaymentOnline(createPaymentDto, clientIp);
        return BaseUtils.dataResponse("Create url payment success", urlPayment, HttpStatus.OK);
    }

    @PostMapping("/pay-cash")
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.USER_WRITE_PAYMENT + "')")
    public ResponseEntity<?> createPaymentCash(@RequestBody List<String> bookingIds) {
        paymentService.createPaymentCash(bookingIds);
        return BaseUtils.baseResponse("Create cash payment success", HttpStatus.OK);
    }
}
