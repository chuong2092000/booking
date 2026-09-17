package project.paymentservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.commondto.dto.DataResponse;
import project.commondto.dto.payment.vnpay.VnPayResponse;
import project.commondto.exception.BusinessException;
import project.paymentservice.client.PaymentGatewayClient;

import java.time.Duration;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class VnPayClientImpl implements PaymentGatewayClient {

    private final RestTemplate restTemplate;

    @Override
    public VnPayResponse refundPayment(Map<String, Object> request) {
        String url = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

        // 1. Khởi tạo Headers (VNPAY thường yêu cầu Content-Type là JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Gói request body và headers vào HttpEntity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);

        // 3. Gọi API bằng RestTemplate.exchange để ép kiểu Generic DataResponse<VnPayResponse>
        ResponseEntity<VnPayResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<VnPayResponse>() {}
            );
        } catch (Exception e) {
            // Bắt các lỗi HTTP (như 4xx, 5xx) hoặc lỗi kết nối
            throw new BusinessException("Error calling VNPay refund API: " + e.getMessage());
        }

       VnPayResponse response = responseEntity.getBody();

        // 4. Validate dữ liệu trả về giống code cũ
        if (response == null) {
            throw new BusinessException("Cannot refund payment: empty response from VNPay");
        }

        return response;
    }
}
