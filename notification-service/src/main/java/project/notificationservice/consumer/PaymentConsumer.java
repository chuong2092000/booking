package project.notificationservice.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.BookingResponse;
import project.commondto.dto.notification.MailBookingCanceled;
import project.commondto.dto.notification.MailPaymentSuccess;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.user.UserResponse;
import project.commonutils.BaseUtils;
import project.notificationservice.client.UserClient;
import project.notificationservice.service.MailService;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final MailService mailService;

    private final ObjectMapper objectMapper;

    private final UserClient userClient;

    @KafkaListener(topics = KafkaTopic.NOTIFICATION_PAYMENT_SUCCESS)
    @RetryableTopic(attempts = "1")
    public void consumePaymentSuccess(String message) throws InterruptedException {
        log.info("Processing NOTIFICATION_PAYMENT_SUCCESS event: push notification payment success. Message: {}", message);
        OutBoxEventPayloadPaymentDto eventPayload = parseOutboxEventPaymentDto(message);
        try {
            PaymentDto paymentDto = eventPayload.paymentDto();

            BaseUtils.validateObject(paymentDto, "Payment DTO", false);
            BaseUtils.validateObject(paymentDto.getKeycloakId(), "User", false);

            UserResponse userResponse = userClient.getUserById(paymentDto.getKeycloakId());
            if (userResponse != null) {
                MailPaymentSuccess mailPaymentSuccess = MailPaymentSuccess.builder()
                        .bookingId(paymentDto.getBookingId())
                        .customerName(userResponse.getFirstName() + " " + userResponse.getLastName())
                        .customerId(paymentDto.getKeycloakId())
                        .transactionCode(paymentDto.getTransactionCode())
                        .paymentMethod(paymentDto.getPaymentMethod())
                        .paidAt(paymentDto.getPaidAt())
                        .amount(paymentDto.getAmount())
                        .build();
                String mailPaymentSuccessStr = mailService.buildEmailPaymentSuccess(mailPaymentSuccess);
                mailService.send(userResponse.getEmail(), mailPaymentSuccessStr, "Thanh toán thành công");
            } else {
                log.warn("User info unavailable, sending notification with limited info");
            }
        } catch (Exception e) {
            log.error("Failed to push notification payment success refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }

    }

    @KafkaListener(topics = KafkaTopic.NOTIFICATION_PAYMENT_REFUNDED)
    @RetryableTopic(attempts = "1")
    public void consumePaymentRefunded(String message) throws InterruptedException {
            // này dùng để lắng gnhe khi call API query của VNPAY và cập nhật r thông báo cho user
    }

    @KafkaListener(topics = KafkaTopic.NOTIFICATION_PAYMENT_REFUNDING)
    @RetryableTopic(attempts = "1")
    public void consumePaymentRefunding(String message) throws InterruptedException {
        String s = mailService.buildEmailPaymentRefunding(null);
        mailService.send("chuonggiang2209@gmail.com", s, "Hoàn tiền thanh toán");
    }

    private OutBoxEventPayloadPaymentDto parseOutboxEventPaymentDto(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        PaymentDto paymentDto = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<PaymentDto>() {
                });
        return new OutBoxEventPayloadPaymentDto(outBoxDto.getId(), paymentDto);
    }

    private record OutBoxEventPayloadPaymentDto(String refId, PaymentDto paymentDto) {
    }
}
