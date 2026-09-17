package project.bookingservice.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import project.bookingservice.processor.ProcessorService;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.commondto.dto.payment.PaymentDto;
import project.commondto.dto.payment.outbox.BookingPaymentDto;

import java.util.List;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelConsumer {

    private final ProcessorService processorService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.ROOM_DECREASED)
    @RetryableTopic(attempts = "1")
    public void consumeRoomDecreased(String message) {
        log.info("Processing ROOM_DECREASED event: locking booking inventory. Message: {}", message);
        OutBoxEventPayloadResult eventPayload = parseOutboxEventResults(message);
        try {
            processorService.lockOrReleaseBookingInv(eventPayload.refId(), eventPayload.resultBookings(), true);
        } catch (Exception e) {
            log.error("Failed to lock booking inventory for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.ROOM_INCREASED)
    @RetryableTopic(attempts = "1")
    public void consumeRoomIncreased(String message) {
        log.info("Processing ROOM_INCREASED event: releasing booking inventory. Message: {}", message);
        OutBoxEventPayloadResult eventPayload = parseOutboxEventResults(message);
        try {
            processorService.lockOrReleaseBookingInv(eventPayload.refId(), eventPayload.resultBookings(), false);
        } catch (Exception e) {
            log.error("Failed to release booking inventory for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.BOOKING_CREATED + "-dlt")
    public void consumeBookingCreatedDlt(String message) {
        log.info("Processing BOOKING_CREATED-DLT event: updating booking status to FAILED. Message: {}", message);
        try {
            OutBoxEventPayloadResult eventPayload = parseOutboxEventResults(message);
            processorService.processBookingFailed(eventPayload.refId(), eventPayload.resultBookings());
        } catch (Exception e) {
            log.error("Room deduction failed, and subsequent booking update to FAILED also failed for message: {}. " +
                    "MANUAL INTERVENTION REQUIRED! Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PROCESSING_PAID)
    @RetryableTopic(attempts = "1")
    public void consumePaymentCreated(String message) {
        log.info("Processing PAYMENT_PROCESSING_PAID event: attach payment info to booking. Message: {}", message);
        OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
        try {
            processorService.processAddPaymentInfo(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to attach payment info for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PAID)
    @RetryableTopic(attempts = "1")
    public void consumePaymentPaid(String message) {
        log.info("Processing PAYMENT_PAID event: confirm booking. Message: {}", message);
        OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
        try {
            processorService.processBookingConfirmedAndPaid(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to confirm booking for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_PAID_FAILED)
    @RetryableTopic(attempts = "1")
    public void consumePaymentFailed(String message) {
        log.info("Processing PAYMENT_FAILED event: mark payment failed on booking. Message: {}", message);
        OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
        try {
            processorService.processFailedPayment(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to mark payment failed for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.ROOM_PROCESSING_INCREASE + "-dlt")
    public void consumeRoomProcessingIncDLT(String message,
                                            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
                                            @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
                                            @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("DATA INCONSISTENCY ALERT - MESSAGE MOVED TO DLT AFTER ALL RETRIES EXHAUSTED. " +
                        "Room inventory was NOT increased, manual reconciliation required immediately. " +
                        "Original topic: {}, offset: {}, failure reason: {}, message: {}",
                originalTopic, offset, exceptionMessage, message);

        // bắn mail để thông báo cho dev team
    }

    @KafkaListener(topics = KafkaTopic.BOOKING_CANCELED + "-dlt")
    public void consumeBookingCanceledDlt(String message) {
        log.warn("Processing BOOKING_CANCELED-DLT event: refund request repeatedly failed. Message: {}", message);
        try {
            OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
            log.error("Refund could not be initiated for refId: {}. MANUAL INTERVENTION REQUIRED!",
                    eventPayload.refId());
            processorService.processPaymentRefundFailed(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to even parse BOOKING_CANCELED-DLT message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_REFUNDED)
    @RetryableTopic(attempts = "1")
    public void consumePaymentRefunded(String message) {
        log.info("Processing PAYMENT_REFUNDED event: mark booking as refunded. Message: {}", message);
        OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
        try {
            processorService.processPaymentRefunded(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to mark booking as refunded for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_REFUNDED_FAILED)
    @RetryableTopic(attempts = "1")
    public void consumePaymentRefundFailed(String message) {
        log.info("Processing PAYMENT_REFUNDED_FAILED event: mark booking refund as failed. Message: {}", message);
        OutBoxEventPayloadBookingPayment eventPayload = parseOutboxEventBookingPaymentDto(message);
        try {
            processorService.processPaymentRefundFailed(eventPayload.refId(), eventPayload.bookingPaymentsDto());
        } catch (Exception e) {
            log.error("Failed to mark booking refund as failed for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.PAYMENT_CASH_UPDATE_PAID)
    @RetryableTopic(attempts = "1")
    public void consumePaymentCashUpdatePaid(String message) {
        log.info("Processing PAYMENT_CASH_UPDATE_PAID event: mark booking as paid (cash). Message: {}", message);
        OutBoxEventPayloadPaymentDto eventPayload = parseOutboxEventPaymentDto(message);
        try {
            processorService.processPaymentCashPaid(eventPayload.refId(), eventPayload.paymentDto());
        } catch (Exception e) {
            log.error("Failed to mark booking as paid (cash) for refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    private OutBoxEventPayloadResult parseOutboxEventResults(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        List<ResultBookingsDto> resultBookings = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<List<ResultBookingsDto>>() {
                });
        return new OutBoxEventPayloadResult(outBoxDto.getId(), resultBookings);
    }

    private OutBoxEventPayloadBookingPayment parseOutboxEventBookingPaymentDto(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        List<BookingPaymentDto> bookingPaymentsDto = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<List<BookingPaymentDto>>() {
                });
        return new OutBoxEventPayloadBookingPayment(outBoxDto.getId(), bookingPaymentsDto);
    }

    private OutBoxEventPayloadPaymentDto parseOutboxEventPaymentDto(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        PaymentDto paymentDto = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<PaymentDto>() {
                });
        return new OutBoxEventPayloadPaymentDto(outBoxDto.getId(), paymentDto);
    }

    private record OutBoxEventPayloadResult(String refId, List<ResultBookingsDto> resultBookings) {
    }

    private record OutBoxEventPayloadBookingPayment(String refId, List<BookingPaymentDto> bookingPaymentsDto) {
    }

    private record OutBoxEventPayloadPaymentDto(String refId, PaymentDto paymentDto) {
    }
}