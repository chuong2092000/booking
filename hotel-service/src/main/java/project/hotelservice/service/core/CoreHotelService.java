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
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.dto.request.CreateHotelDto;
import project.commondto.dto.hotel.dto.request.CreateImageDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.repo.HotelRepo;
import project.hotelservice.service.impl.OutboxService;
import project.hotelservice.spec.HotelSpec;
import project.hotelservice.utils.S3UploadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreHotelService {

    private final HotelRepo hotelRepo;

    private final CoreHotelImageService coreHotelImageService;

    private final S3UploadService s3UploadService;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    public Page<Hotel> getHotels(SearchHotelDto searchHotelDto,
                                 Pageable pageable,
                                 String ownerHotelId, boolean isApproval) {
        Specification<Hotel> hotelSpecification = HotelSpec.isDeleted(false)
                .and(HotelSpec.hasKeycloakId(ownerHotelId))
                .and(HotelSpec.hasName(searchHotelDto.getName()))
                .and(HotelSpec.hasAddress(searchHotelDto.getAddress()))
                .and(HotelSpec.isApproval(isApproval));
        return hotelRepo.findAll(hotelSpecification, pageable);
    }

    public Hotel getHotelFromOption(String hotelId, Optional<Hotel> opHotel) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        if (opHotel.isEmpty()) {
            log.warn("Hotel not found with id:  {}", hotelId);
            throw new BusinessException("Hotel not found with id: " + hotelId);
        }
        Hotel hotel = opHotel.get();
        log.info("Hotel founded with id:  {}", hotel.getId());
        return hotel;
    }

    public Hotel createHotel(CreateHotelDto createHotelDto, String ownerHotelId, MultipartFile mainImageFile, MultipartFile[] supportImageFiles) {

        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);
        BaseUtils.validateObject(createHotelDto, "Create hotel DTO", false);

        BaseUtils.validateObject(createHotelDto.getName(), "Hotel name", false);
        BaseUtils.validateObject(createHotelDto.getAddress(), "Address", false);
        BaseUtils.validateObject(createHotelDto.getDescription(), "Description", false);


        BaseUtils.validateObject(mainImageFile, "Main image file", false);

        BaseUtils.validateObject(supportImageFiles, "Support image files", false);

        String uri = s3UploadService.getHostUrl();

        UUID randomKeyMain = UUID.randomUUID();
        String mainKey = s3UploadService.buildKey(S3UploadService.Folder.HOTEL, randomKeyMain, mainImageFile);
        List<CreateImageDto> supportImages = new ArrayList<>();
        byte[] mainByte;

        try {
            mainByte = mainImageFile.getBytes();
            for (MultipartFile file : supportImageFiles) {
                UUID randomKeySupport = UUID.randomUUID();
                String supportKey = s3UploadService.buildKey(S3UploadService.Folder.HOTEL, randomKeySupport, file);
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

        Hotel hotel = Hotel.builder()
                .name(createHotelDto.getName())
                .address(createHotelDto.getAddress())
                .description(createHotelDto.getDescription())
                .keycloakId(ownerHotelId)
                .imageUri(uri)
                .imageName(mainKey)
                .imageByte(mainByte)
                .build();

        Hotel hotelSaved = hotelRepo.save(hotel);
        String hotelSavedId = hotelSaved.getId();
        coreHotelImageService.createHotelImage(supportImages, hotelSavedId);

        OutBoxDto outBoxDto = OutBoxDto.builder()
                .topic(KafkaTopic.S3_IMAGES_HOTEL)
                .payload(BaseUtils.convertObjectToString(objectMapper, hotelSavedId))
                .build();

        outboxService.createOutbox(outBoxDto);

        return hotelSaved;
    }

    public void existsHotelByOwnerHotelId(String hotelId, String ownerHotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);

        boolean exists = hotelRepo.existsByIdAndKeycloakIdAndIsDeleted(hotelId, ownerHotelId, false);

        if (!exists) {
            log.warn("Hotel not found with id:  {}", hotelId);
            throw new BusinessException("Hotel not found with id: " + hotelId);
        }
    }

    public Hotel getHotelById(String id) {
        BaseUtils.validateObject(id, "Hotel id", false);
        return hotelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Hotel not found for ID: " + id
                ));
    }
}