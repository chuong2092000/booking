package project.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.payment.CreatePaymentDto;
import project.commonutils.BaseUtils;
import project.paymentservice.orchestrator.OrchestratorService;
import project.paymentservice.service.PaymentService;
import project.paymentservice.service.core.CorePaymentService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CorePaymentService corePaymentService;

    private final OrchestratorService orchestratorService;

    @Override
    public String createPaymentOnline(CreatePaymentDto createPaymentDto, String ipAddress) {
        BaseUtils.validateObject(createPaymentDto, "Create payment DTO", false);
        BaseUtils.validateListObject(createPaymentDto.getBookingIds(), "Booking IDS", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentMethod(), "Payment method", false);
        BaseUtils.validateObject(createPaymentDto.getPaymentGateway(), "Payment gateway", false);
        BaseUtils.validateObject(ipAddress, "Ip Address", false);

        return corePaymentService.withLocks("booking-payment", createPaymentDto.getBookingIds(), () -> orchestratorService.createPaymentOnline(createPaymentDto, ipAddress));
    }

    @Override
    public void createPaymentCash(List<String> bookingIds) {
        BaseUtils.validateListObject(bookingIds, "Booking IDS", false);
        corePaymentService.withLocks("booking-payment", bookingIds, () -> {
            orchestratorService.createPaymentCash(bookingIds);
            return null;
        });
    }


    @Override
    public void processRefundPayment(List<String> paymentIds) {
        BaseUtils.validateListObject(paymentIds, "Payment IDS", false);
        corePaymentService.withLocks("payment", paymentIds, () -> {
            orchestratorService.processRefundPayment(paymentIds);
            return null;
        });
    }

    @Transactional
    @Override
    public void queryUpdateRefundedPayment() {

    }

    @Override
    public void updatePaidCashPayment(String paymentId) {
        BaseUtils.validateObject(paymentId, "Payment id", false);
        corePaymentService.withLocks("payment", List.of(paymentId), () -> {
            orchestratorService.updatePaidCashPayment(paymentId);
            return null;
        });
    }
}