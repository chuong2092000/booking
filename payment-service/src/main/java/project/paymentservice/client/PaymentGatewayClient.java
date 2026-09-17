package project.paymentservice.client;

import project.commondto.dto.payment.vnpay.VnPayResponse;

import java.util.Map;

public interface PaymentGatewayClient {
    VnPayResponse refundPayment(Map<String, Object> request);
}
