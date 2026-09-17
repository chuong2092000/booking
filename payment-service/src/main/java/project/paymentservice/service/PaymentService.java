package project.paymentservice.service;

import project.commondto.dto.payment.CreatePaymentDto;

import java.util.List;

public interface PaymentService {
    String createPaymentOnline(CreatePaymentDto createPaymentDto, String ipAddress);

    void createPaymentCash(List<String> bookingIds);

    void processRefundPayment(List<String> paymentIds);

    void queryUpdateRefundedPayment();

    void updatePaidCashPayment(String paymentId);
}