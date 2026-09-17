package project.paymentservice.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.paymentservice.processor.ProcessorService;

import java.util.List;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final ProcessorService processorService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = KafkaTopic.PAYMENT_PROCESSING_REFUND)
    @RetryableTopic(attempts = "1")
    public void consumePaymentRefund(String message) {
        log.info("Processing PAYMENT_PROCESSING_REFUND event: refund payment. Message: {}", message);
        OutBoxEventPayloadPaymentDto eventPayload = parseOutboxEventPayment(message);
        try {
            processorService.refundPayment(eventPayload.refId(), eventPayload.paymentDto());
        } catch (Exception e) {
            log.error("Failed to refund payment for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PROCESSING_REFUND + "-dlt")
    public void consumePaymentRefundDlt(String message) {
        log.warn("Processing PAYMENT_PROCESSING_REFUND-DLT event: refund call repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadPaymentDto eventPayload = parseOutboxEventPayment(message);
            log.error("Refund could not be processed for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_PROCESSING_REFUND-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PROCESSING_PAID + "-dlt")
    public void consumePaymentProcessingPaidDlt(String message) {
        log.warn("Processing PAYMENT_PROCESSING_PAID-DLT event: attach payment info repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Could not attach payment info to booking for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_PROCESSING_PAID-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PAID + "-dlt")
    public void consumePaymentPaidDlt(String message) {
        log.warn("Processing PAYMENT_PAID-DLT event: confirm booking repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Payment succeeded but booking confirm failed for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_PAID-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PAID_FAILED + "-dlt")
    public void consumePaymentPaidFailedDlt(String message) {
        log.warn("Processing PAYMENT_PAID_FAILED-DLT event: mark payment failed repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Could not mark payment failed on booking for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_PAID_FAILED-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_REFUNDED + "-dlt")
    public void consumePaymentRefundedDlt(String message) {
        log.warn("Processing PAYMENT_REFUNDED-DLT event: mark booking refunded repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Payment was refunded at gateway but booking status update failed for refId: {}. " +
                    "MANUAL INTERVENTION REQUIRED!", eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_REFUNDED-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_REFUNDED_FAILED + "-dlt")
    public void consumePaymentRefundFailedDlt(String message) {
        log.warn("Processing PAYMENT_REFUNDED_FAILED-DLT event: mark booking refund failed repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Could not mark booking refund as failed for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
        } catch (Exception e) {
            log.error("Failed to even parse PAYMENT_REFUNDED_FAILED-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    private OutBoxEventPayloadPaymentDto parseOutboxEventPayment(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {});
        PaymentDto paymentDto = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<PaymentDto>() {});
        return new OutBoxEventPayloadPaymentDto(outBoxDto.getId(), paymentDto);
    }

    private OutBoxEventPayloadBookingPayment parseOutboxEventBookingPaymentDto(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {});
        List<BookingPaymentDto> bookingPaymentsDto = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<List<BookingPaymentDto>>() {});
        return new OutBoxEventPayloadBookingPayment(outBoxDto.getId(), bookingPaymentsDto);
    }

    private record OutBoxEventPayloadPaymentDto(String refId, PaymentDto paymentDto) {}
    private record OutBoxEventPayloadBookingPayment(String refId, List<BookingPaymentDto> bookingPaymentsDto) {}
}
