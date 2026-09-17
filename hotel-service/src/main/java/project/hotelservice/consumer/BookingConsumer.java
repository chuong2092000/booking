package project.hotelservice.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.booking.outbox.ResultBookingsDto;
import project.hotelservice.processor.ProcessorService;

import java.util.List;

import static project.commonutils.BaseUtils.convertStringToObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingConsumer {

    private final ProcessorService processorService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.BOOKING_CREATED)
    @RetryableTopic(attempts = "1")
    public void consumeBookingCreated(String message) throws InterruptedException {
        log.info("Processing BOOKING_CREATED event: holding room inventory. Message: {}", message);
        try {
            handleWithStatusHold(message, true);
        } catch (Exception e) {
            log.error("Failed to hold room inventory for message: {}. Error: {}", message, e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.ROOM_DECREASED + "-dlt")
    public void consumeRoomDecreasedDlt(String message) {
        log.info("Processing ROOM_DECREASED-DLT event: reverting room inventory. Message: {}", message);
        try {
            handleWithStatusHold(message, false);
        } catch (Exception e) {
            log.error("DATA INCONSISTENCY ALERT: Failed to revert room inventory for message: {}. " +
                    "MANUAL INTERVENTION REQUIRED! Error: {}", message, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopic.ROOM_PROCESSING_INCREASE)
    @RetryableTopic(attempts = "1")
    public void consumeRoomProcessingInc(String message) throws InterruptedException {
        log.info("Processing ROOM_PROCESSING_INCREASE event: increasing room inventory. Message: {}", message);
        try {
            handleWithStatusHold(message, false);
        } catch (Exception e) {
            log.error("DATA INCONSISTENCY ALERT: Failed to increase room inventory for message: {}. " +
                    "MANUAL INTERVENTION REQUIRED! Error: {}", message, e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.ROOM_INCREASED + "-dlt")
    public void consumeRoomIncreasedDlt(String message) {
        log.error("DATA INCONSISTENCY ALERT: Room revert (increase) failed after retries for message: {}. " +
                "Room inventory may already be reverted while booking status was not updated accordingly. " +
                "MANUAL INTERVENTION REQUIRED!", message);
        // co the send mail cho DOI van hanh de fix
    }

    @KafkaListener(topics = KafkaTopic.S3_IMAGES_HOTEL)
    @RetryableTopic(attempts = "1")
    public void consumeProcessS3ImageHotel(String message) throws InterruptedException {
        log.info("Processing S3_IMAGES_HOTEL event: Upload S3 AWS. Message: {}", message);
        try {
            OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
            });

            String hotelId = convertStringToObject(objectMapper,outBoxDto.getPayload(),new TypeReference<String>() {
            });
            processorService.uploadS3FileAwsHotel(outBoxDto.getId(), hotelId);
        } catch (Exception e) {
            log.error("Failed to upload S3 image hotel for message: {}. Error: {}", message, e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopic.S3_IMAGES_ROOM_TYPE)
    @RetryableTopic(attempts = "1")
    public void consumeProcessS3ImageRoomType(String message) throws InterruptedException {
        log.info("Processing S3_IMAGES_ROOM_TYPE event: Upload S3 AWS. Message: {}", message);
        try {
            OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
            });

            String roomTypeId = convertStringToObject(objectMapper,outBoxDto.getPayload(),new TypeReference<String>() {
            });
            processorService.uploadS3FileAwsRoomType(outBoxDto.getId(), roomTypeId);
        } catch (Exception e) {
            log.error("Failed to upload S3 image hotel for message: {}. Error: {}", message, e.getMessage(), e);
            throw e;
        }
    }

    private void handleWithStatusHold(String message, boolean hold) throws InterruptedException {
        OutBoxDto outBoxDto = convertStringToObject(objectMapper, message, new TypeReference<OutBoxDto>() {
        });
        List<ResultBookingsDto> resultBookings = convertStringToObject(
                objectMapper, outBoxDto.getPayload(), new TypeReference<List<ResultBookingsDto>>() {
                });
        processorService.processQuantityRoomInventory(outBoxDto.getId(), resultBookings, hold);
    }
}