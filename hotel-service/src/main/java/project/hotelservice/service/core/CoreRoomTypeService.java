package project.hotelservice.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.KafkaTopic;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.request.CreateImageDto;
import project.commondto.dto.hotel.dto.request.CreateRoomTypeDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.entity.RoomType;
import project.hotelservice.repo.RoomInventoryRepo;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.impl.OutboxService;
import project.hotelservice.spec.RoomTypeSpec;
import project.hotelservice.utils.S3UploadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreRoomTypeService {

    private final RoomTypeRepo roomTypeRepo;

    private final CoreHotelService coreHotelService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    private final S3UploadService s3UploadService;

    private final RoomInventoryRepo roomInventoryRepo;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    public Page<RoomType> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, Pageable pageable, String hotelId, boolean isApproval) {
        Specification<RoomType> roomTypeSpec =
                RoomTypeSpec.hasHotelId(hotelId)
                        .and(RoomTypeSpec.isDeleted(false))
                        .and(RoomTypeSpec.isApproval(isApproval))
                        .and(RoomTypeSpec.hasName(searchRoomTypesDto.getName()));
        return roomTypeRepo.findAll(roomTypeSpec, pageable);
    }

    public List<RoomType> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, String hotelId, boolean isApproval) {
        Specification<RoomType> roomTypeSpec =
                RoomTypeSpec.hasHotelId(hotelId)
                        .and(RoomTypeSpec.isDeleted(false))
                        .and(RoomTypeSpec.isApproval(isApproval))
                        .and(RoomTypeSpec.hasName(searchRoomTypesDto.getName()));
        return roomTypeRepo.findAll(roomTypeSpec);
    }

    public void deleteRoomTypeByHotelId(String hotelId) {
        BaseUtils.validateObject(hotelId, "Hotel id", false);
        roomTypeRepo.deleteRoomTypeByHotelId(hotelId);
    }

    public void deleteRoomTypeById(String id) {
        BaseUtils.validateObject(id, "Room type id", false);
        if (roomTypeRepo.deleteRoomTypeById(id) <= 0) {
            log.warn("Delete room type failed with id: {}", id);
            throw new BusinessException("Room type cannot delete with id: " + id);
        }
    }

    public void deleteRoomTypeByIds(List<String> ids) {
        BaseUtils.validateObject(ids, "Room type ids", false);
        if (roomTypeRepo.deleteRoomTypeByIds(ids) <= 0) {
            log.warn("Delete room type failed with ids: {}", ids);
            throw new BusinessException("Room type cannot delete with ids: " + ids);
        }
    }

    public RoomType getRoomTypeFromOption(String roomTypeId, Optional<RoomType> opRoomType) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);

        if (opRoomType.isEmpty()) {
            log.warn("Room type not found with id:  {}", roomTypeId);
            throw new BusinessException("Room type not found with id: " + roomTypeId);
        }

        RoomType roomType = opRoomType.get();
        log.info("Room type founded with id:  {}", roomType.getId());

        return roomType;
    }

    public RoomType createRoomType(String ownerHotelId, String hotelId, CreateRoomTypeDto createRoomTypeDto, MultipartFile mainImageFile, MultipartFile[] supportImageFiles) {

        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        BaseUtils.validateObject(hotelId, "Hotel id", false);
        BaseUtils.validateObject(createRoomTypeDto, "Create room type DTO", false);

        BaseUtils.validateObject(createRoomTypeDto.getName(), "Room type name", false);
        BaseUtils.validateObject(createRoomTypeDto.getName(), "Room type description", false);

        BaseUtils.validateObject(createRoomTypeDto.getFromDate(), "Room type from date", false);
        BaseUtils.validateObject(createRoomTypeDto.getToDate(), "Room type to date", false);
        BaseUtils.validateObject(createRoomTypeDto.getQuantity(), "Room type quantity", false);


        BaseUtils.validateObject(mainImageFile, "Main image file", false);

        BaseUtils.validateObject(supportImageFiles, "Support image files", false);

        coreHotelService.existsHotelByOwnerHotelId(hotelId, ownerHotelId);

        String uri = s3UploadService.getHostUrl();

        UUID randomKeyMain = UUID.randomUUID();
        String mainKey = s3UploadService.buildKey(S3UploadService.Folder.ROOM_TYPE, randomKeyMain, mainImageFile);
        List<CreateImageDto> supportImages = new ArrayList<>();
        byte[] mainByte;

        try {
            mainByte = mainImageFile.getBytes();
            for (MultipartFile file : supportImageFiles) {
                UUID randomKeySupport = UUID.randomUUID();
                String supportKey = s3UploadService.buildKey(S3UploadService.Folder.ROOM_TYPE, randomKeySupport, file);
                CreateImageDto imageDto = CreateImageDto.builder()
                        .uri(uri)
                        .name(supportKey)
                        .file(file.getBytes())
                        .build();
                supportImages.add(imageDto);
            }
        } catch (IOException e) {
            throw new BusinessException("Error get binaries in files!");
        }

        RoomType roomType = RoomType.builder()
                .name(createRoomTypeDto.getName())
                .description(createRoomTypeDto.getDescription())
                .hotelId(hotelId)
                .imageUri(uri)
                .imageName(mainKey)
                .imageByte(mainByte)
                .build();

        RoomType roomTypeSaved = roomTypeRepo.save(roomType);
        String roomTypeId = roomTypeSaved.getId();

        LocalDate from = createRoomTypeDto.getFromDate();
        LocalDate to = createRoomTypeDto.getToDate();

        List<RoomInventory> inventories = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            RoomInventory roomInventory = RoomInventory.builder()
                    .roomTypeId(roomTypeId)
                    .price(createRoomTypeDto.getPrice())
                    .availableQuantity(createRoomTypeDto.getQuantity())
                    .totalQuantity(createRoomTypeDto.getQuantity())
                    .date(date)
                    .build();
            inventories.add(roomInventory);
        }

        roomInventoryRepo.saveAll(inventories);
        coreRoomTypeImageService.createRoomTypeImage(supportImages, roomTypeId);

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .topic(KafkaTopic.S3_IMAGES_ROOM_TYPE)
                .payload(BaseUtils.convertObjectToString(objectMapper, roomTypeId))
                .build();
        outboxService.createOutbox(outBoxDto);
        return roomTypeSaved;
    }

    public RoomType getRoomTypeById(String id) {
        BaseUtils.validateObject(id, "Room-type id", false);
        return roomTypeRepo.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Room-type not found for ID: " + id
                ));
    }
}
