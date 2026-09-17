package project.hotelservice.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.commondto.dto.hotel.dto.request.CreateImageDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.HotelImage;
import project.hotelservice.repo.HotelImageRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreHotelImageService {

    private final HotelImageRepo hotelImageRepo;

    public List<HotelImage> getHotelImagesByHoTelId(String hotelId) {
        BaseUtils.validateObject(hotelId, "Hotel id", false);
        return hotelImageRepo.getHotelImagesByHotelIdAndIsDeleted(hotelId, false);
    }

    public List<HotelImage> createHotelImage(List<CreateImageDto> createImagesDto, String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);
        BaseUtils.validateListObject(createImagesDto, "Create images DTO", false);

        List<HotelImage> hotelImages = new ArrayList<>();

        for (CreateImageDto imageDto : createImagesDto) {
            BaseUtils.validateObject(imageDto.getName(), "Name image", false);
            BaseUtils.validateObject(imageDto.getUri(), "Uri image", false);

            HotelImage hotelImage = HotelImage.builder()
                    .hotelId(hotelId)
                    .name(imageDto.getName())
                    .uri(imageDto.getUri())
                    .imageByte(imageDto.getFile())
                    .build();
            hotelImages.add(hotelImage);
        }

        return hotelImageRepo.saveAll(hotelImages);
    }

    public void deleteHotelImageByHotelId(String hotelId) {
        BaseUtils.validateObject(hotelId, "Hotel id", false);
        hotelImageRepo.deleteHotelImageByHotelId(hotelId);
    }

    public void deleteHotelImageById(String id) {
        BaseUtils.validateObject(id, "Hotel image id", false);
        if (hotelImageRepo.deleteHotelImageById(id) <= 0) {
            log.warn("Delete hotel image failed with id: {}", id);
            throw new BusinessException("Hotel image cannot delete with id: " + id);
        }
    }

    public void deleteHotelImageByIds(List<String> ids) {
        BaseUtils.validateObject(ids, "Hotel image ids", false);
        if (hotelImageRepo.deleteHotelImageByIds(ids) <= 0) {
            log.warn("Delete hotel image failed with ids: {}", ids);
            throw new BusinessException("Hotel image cannot delete with ids: " + ids);
        }
    }

    public List<HotelImage> getHotelImagesByHotelIdAndImageByte(String hotelId) {
        BaseUtils.validateObject(hotelId, "Hotel id", false);
        return hotelImageRepo.getHotelImagesByHotelIdAndImageByteNotNull(hotelId);
    }
}
