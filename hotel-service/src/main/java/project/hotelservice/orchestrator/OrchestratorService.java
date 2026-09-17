package project.hotelservice.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.ProcessedEventDto;
import project.commondto.dto.ProcessedEventStatus;
import project.commondto.dto.booking.outbox.BookingRoomInvDto;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.entity.HotelImage;
import project.hotelservice.entity.RoomType;
import project.hotelservice.entity.RoomTypeImage;
import project.hotelservice.service.InternalService;
import project.hotelservice.service.core.CoreHotelImageService;
import project.hotelservice.service.core.CoreHotelService;
import project.hotelservice.service.core.CoreRoomTypeImageService;
import project.hotelservice.service.core.CoreRoomTypeService;
import project.hotelservice.service.impl.ProcessedEventService;
import project.hotelservice.utils.ByteArrayMultipartFile;
import project.hotelservice.utils.S3UploadService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {

    private final ProcessedEventService processedEventService;

    private final InternalService internalService;

    private final ObjectMapper objectMapper;

    private final S3UploadService s3UploadService;

    private final CoreHotelService coreHotelService;

    private final CoreHotelImageService coreHotelImageService;

    private final CoreRoomTypeService coreRoomTypeService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    @Transactional
    public void decreaseInventoryAndSaveEvent(String refId, List<String> bookingIds, List<BookingRoomInvDto> roomInvDtos) {

        BaseUtils.validateListObject(roomInvDtos, "Booking room inv", false);
        BaseUtils.validateObject(refId, "Ref id", false);

        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type("Decrease room inventories")
                .value(BaseUtils.convertObjectToString(objectMapper, roomInvDtos))
                .build();

        try {
            internalService.decreaseQuantity(bookingIds, roomInvDtos);
            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
        } catch (Exception e) {

            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}", failedEvent.getId(), failedEvent.getRefId(),
                    failedEvent.getErrorMessage());
            throw e;
        }
    }

    @Transactional
    public void increaseInventoryAndSaveEvent(String refId, List<String> bookingIds, List<BookingRoomInvDto> roomInvDtos) {

        BaseUtils.validateListObject(roomInvDtos, "Booking room inv", false);
        BaseUtils.validateObject(refId, "Ref id", false);

        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type("Increase room inventories")
                .value(BaseUtils.convertObjectToString(objectMapper, roomInvDtos))
                .build();

        try {
            internalService.increaseQuantity(bookingIds, roomInvDtos);
            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
        } catch (Exception e) {

            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}", failedEvent.getId(), failedEvent.getRefId(),
                    failedEvent.getErrorMessage());
            throw e;
        }
    }

    @Transactional
    public void updloadS3FileAwsHotel(String refId, String hotelId) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateObject(hotelId, "Hotel id", false);

        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type("Upload S3 hotel")
                .value(BaseUtils.convertObjectToString(objectMapper, hotelId))
                .build();
        try {

            Hotel hotel = coreHotelService.getHotelById(hotelId);
            List<HotelImage> hotelImages = coreHotelImageService.getHotelImagesByHotelIdAndImageByte(hotelId);
            if (hotel != null && hotel.getImageByte() != null) {
                ByteArrayMultipartFile mainFile = new ByteArrayMultipartFile(
                        hotel.getImageByte(), "main-file", hotel.getName()
                );

                s3UploadService.uploadFile(mainFile, "image/webp", hotel.getImageName());
                hotel.setProcessedIm(true);
                hotel.setImageByte(null);
            }

            if (hotelImages != null && !hotelImages.isEmpty()) {
                for (HotelImage hotelImage : hotelImages) {
                    if (hotelImage.getImageByte() != null) {
                        ByteArrayMultipartFile supportFile = new ByteArrayMultipartFile(
                                hotelImage.getImageByte(), "support-file", hotelImage.getName()
                        );

                        s3UploadService.uploadFile(supportFile, "image/webp", hotelImage.getName());

                        hotelImage.setProcessed(true);
                        hotelImage.setImageByte(null);
                    }
                }
            }

            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
        } catch (Exception e) {

            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}", failedEvent.getId(), failedEvent.getRefId(),
                    failedEvent.getErrorMessage());
            throw e;
        }
    }

    @Transactional
    public void updloadS3FileAwsRoomType(String refId, String roomTypeId) {
        BaseUtils.validateObject(refId, "Ref id", false);
        BaseUtils.validateObject(roomTypeId, "Room-type id", false);

        ProcessedEventDto processedEventDto = ProcessedEventDto.builder()
                .refId(refId)
                .type("Upload S3 room-type")
                .value(BaseUtils.convertObjectToString(objectMapper, roomTypeId))
                .build();
        try {

            RoomType roomType = coreRoomTypeService.getRoomTypeById(roomTypeId);
            List<RoomTypeImage> roomTypeImages = coreRoomTypeImageService.getRoomTypeImagesByRoomTypeIdAndImageByte(roomTypeId);
            if (roomType != null && roomType.getImageByte() != null) {
                ByteArrayMultipartFile mainFile = new ByteArrayMultipartFile(
                        roomType.getImageByte(), "main-file", roomType.getName()
                );

                s3UploadService.uploadFile(mainFile, "image/webp", roomType.getImageName());
                roomType.setProcessedIm(true);
                roomType.setImageByte(null);
            }

            if (roomTypeImages != null && !roomTypeImages.isEmpty()) {
                for (RoomTypeImage roomTypeImage : roomTypeImages) {
                    if (roomTypeImage.getImageByte() != null) {
                        ByteArrayMultipartFile supportFile = new ByteArrayMultipartFile(
                                roomTypeImage.getImageByte(), "support-file", roomTypeImage.getName()
                        );

                        s3UploadService.uploadFile(supportFile, "image/webp", roomTypeImage.getName());

                        roomTypeImage.setProcessed(true);
                        roomTypeImage.setImageByte(null);
                    }
                }
            }

            processedEventDto.setStatus(ProcessedEventStatus.SUCCESS);
            ProcessedEventDto successEvent = processedEventService.createSuccessEvent(processedEventDto);
            log.info("Event success created with id: {} and refId: {}", successEvent.getId(), successEvent.getRefId());
        } catch (Exception e) {

            processedEventDto.setStatus(ProcessedEventStatus.FAILED);
            processedEventDto.setErrorMessage(e.getMessage());

            ProcessedEventDto failedEvent = processedEventService.createFailedEvent(processedEventDto);
            log.error("Event failed created with id: {} and refId: {}, error: {}", failedEvent.getId(), failedEvent.getRefId(),
                    failedEvent.getErrorMessage());
            throw e;
        }
    }

}