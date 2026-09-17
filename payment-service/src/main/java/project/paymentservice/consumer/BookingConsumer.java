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
import project.commondto.dto.payment.outbox.BookingPaymentDto;
import project.paymentservice.processor.ProcessorService;

import java.util.List;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingConsumer {

    private final ProcessorService processorService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.BOOKING_CANCELED)
    @RetryableTopic(attempts = "1")
    public void consumeBookingCanceled(String message) {
        OutBoxEventPayloadBookingPayment outBoxEventPayloadPayment = parseOutboxEventBookingPayment(message);
        processorService.cancelBookingForRefundPayment(outBoxEventPayloadPayment.refId(),
                outBoxEventPayloadPayment.bookingPaymentDtos());
        log.info("Processing BOOKING_CANCELED event: refund payment. Message: {}", message);
    }

    private OutBoxEventPayloadBookingPayment parseOutboxEventBookingPayment(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        List<BookingPaymentDto> bookingPaymentDtos = convertStringToObject(objectMapper, outBoxDto.getPayload(), new TypeReference<List<BookingPaymentDto>>() {
        });
        return new OutBoxEventPayloadBookingPayment(outBoxDto.getId(), bookingPaymentDtos);
    }

    private record OutBoxEventPayloadBookingPayment(String refId, List<BookingPaymentDto> bookingPaymentDtos) {
    }
}