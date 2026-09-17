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
import project.commondto.dto.notification.MailBookingSuccess;
import project.commondto.dto.user.UserResponse;
import project.commonutils.BaseUtils;
import project.notificationservice.client.UserClient;
import project.notificationservice.service.MailService;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingConsumer {

    private final MailService mailService;

    private final UserClient userClient;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.NOTIFICATION_BOOKING_CONFIRMED)
    @RetryableTopic(attempts = "1")
    public void consumeBookingConfirmed(String message) {
        log.info("Processing NOTIFICATION_BOOKING_CONFIRMED event: push notification booking success. Message: {}", message);
        OutBoxEventPayloadBookingRes eventPayload = parseOutboxEventBookingRes(message);
        try {
            BookingResponse bookingResponse = eventPayload.bookingResponse();


            BaseUtils.validateObject(bookingResponse.getCommon(), "Booking DTO", false);
            BaseUtils.validateListObject(bookingResponse.getDetails(), "Booking details", false);
            BaseUtils.validateObject(bookingResponse.getCommon().getKeycloakId(), "User", false);

            UserResponse userResponse = userClient.getUserById(bookingResponse.getCommon().getKeycloakId());

            if (userResponse != null) {
                MailBookingSuccess mailBookingSuccess = MailBookingSuccess.builder()
                        .bookingId(bookingResponse.getCommon().getId())
                        .customerName(userResponse.getFirstName() + " " + userResponse.getLastName())
                        .bookingCode(bookingResponse.getCommon().getCode())
                        .hotelName(bookingResponse.getCommon().getHotelName())
                        .checkInDate(String.valueOf(bookingResponse.getCommon().getCheckInDate()))
                        .checkOutDate(String.valueOf(bookingResponse.getCommon().getCheckOutDate()))
                        .paymentStatus(bookingResponse.getCommon().getPaymentStatus())
                        .totalPrice(bookingResponse.getCommon().getTotalAmount())
                        .rooms(bookingResponse.getDetails())
                        .build();
                String bookingSuccess = mailService.buildEmailBookingSuccess(mailBookingSuccess);
                mailService.send(userResponse.getEmail(), bookingSuccess, "Đặt phòng thành công");
            } else {
                log.warn("User info unavailable, sending notification with limited info");
            }
        } catch (Exception e) {
            log.error("Failed to push notification booking success refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.NOTIFICATION_BOOKING_CANCELED)
    @RetryableTopic(attempts = "1")
    public void consumeBookingCanceled(String message){
        log.info("Processing NOTIFICATION_BOOKING_CANCELED event: push notification booking canceled. Message: {}", message);
        OutBoxEventPayloadBookingRes eventPayload = parseOutboxEventBookingRes(message);
        try {
            BookingResponse bookingResponse = eventPayload.bookingResponse();


            BaseUtils.validateObject(bookingResponse.getCommon(), "Booking DTO", false);
            BaseUtils.validateListObject(bookingResponse.getDetails(), "Booking details", false);
            BaseUtils.validateObject(bookingResponse.getCommon().getKeycloakId(), "User", false);

            UserResponse userResponse = userClient.getUserById(bookingResponse.getCommon().getKeycloakId());

            if (userResponse != null) {
                MailBookingCanceled mailBookingSuccess = MailBookingCanceled.builder()
                        .bookingId(bookingResponse.getCommon().getId())
                        .customerName(userResponse.getFirstName() + " " + userResponse.getLastName())
                        .bookingCode(bookingResponse.getCommon().getCode())
                        .hotelName(bookingResponse.getCommon().getHotelName())
                        .checkInDate(String.valueOf(bookingResponse.getCommon().getCheckInDate()))
                        .checkOutDate(String.valueOf(bookingResponse.getCommon().getCheckOutDate()))
                        .paymentStatus(bookingResponse.getCommon().getPaymentStatus())
                        .rooms(bookingResponse.getDetails())
                        .build();
                String bookingCanceled = mailService.buildEmailBookingCanceled(mailBookingSuccess);
                mailService.send(userResponse.getEmail(), bookingCanceled, "Hủy phòng thành công");
            } else {
                log.warn("User info unavailable, sending notification with limited info");
            }
        } catch (Exception e) {
            log.error("Failed to push notification booking canceled refId: {}. Error: {}",
                    eventPayload.refId(), e.getMessage(), e);
            throw e;
        }
    }

    private OutBoxEventPayloadBookingRes parseOutboxEventBookingRes(String message) {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        BookingResponse bookingResponse = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<BookingResponse>() {
                });
        return new OutBoxEventPayloadBookingRes(outBoxDto.getId(), bookingResponse);
    }

    private record OutBoxEventPayloadBookingRes(String refId, BookingResponse bookingResponse) {
    }
}
