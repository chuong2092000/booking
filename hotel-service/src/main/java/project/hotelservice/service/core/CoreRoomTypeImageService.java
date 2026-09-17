package project.hotelservice.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.commondto.dto.hotel.dto.request.CreateImageDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.RoomTypeImage;
import project.hotelservice.repo.RoomTypeImageRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreRoomTypeImageService {

    private final RoomTypeImageRepo roomTypeImageRepo;

    public List<RoomTypeImage> createRoomTypeImage(List<CreateImageDto> createImagesDto, String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        BaseUtils.validateListObject(createImagesDto, "Create images DTO", false);

        List<RoomTypeImage> roomTypeImages = new ArrayList<>();

        for (CreateImageDto imageDto : createImagesDto) {
            BaseUtils.validateObject(imageDto.getName(), "Name image", false);
            BaseUtils.validateObject(imageDto.getUri(), "Uri image", false);

            RoomTypeImage roomTypeImage = RoomTypeImage.builder()
                    .roomTypeId(roomTypeId)
                    .name(imageDto.getName())
                    .uri(imageDto.getUri())
                    .imageByte(imageDto.getFile())
                    .build();
            roomTypeImages.add(roomTypeImage);
        }

        return roomTypeImageRepo.saveAll(roomTypeImages);
    }

    public void deleteRoomTypeImageByRoomTypeIds(List<String> roomTypeIds) {
        BaseUtils.validateListObject(roomTypeIds, "Room type ids", false);
        roomTypeImageRepo.deleteRoomTypeImageByRoomTypeIds(roomTypeIds);
    }

    public void deleteRoomTypeImageByRoomTypeId(String roomTypeId) {
        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        roomTypeImageRepo.deleteRoomTypeImageByRoomTypeId(roomTypeId);
    }

    public void deleteRoomTypeImageById(String id) {
        BaseUtils.validateObject(id, "Room type image id", false);
        if (roomTypeImageRepo.deleteRoomTypeImageById(id) <= 0) {
            log.warn("Delete room type image failed with id: {}", id);
            throw new BusinessException("Room type image cannot delete with id: " + id);
        }
    }

    public void deleteRoomTypeImageByIds(List<String> ids) {
        BaseUtils.validateObject(ids, "Room type image ids", false);
        if (roomTypeImageRepo.deleteRoomTypeImageByIds(ids) <= 0) {
            log.warn("Delete room type image failed with ids: {}", ids);
            throw new BusinessException("Room type image cannot delete with ids: " + ids);
        }
    }

    public List<RoomTypeImage> getRoomTypeImagesByRoomTypeId(String roomTypeId) {
        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        return roomTypeImageRepo.getRoomTypeImagesByRoomTypeId(roomTypeId);
    }

    public List<RoomTypeImage> getRoomTypeImagesByRoomTypeIdAndImageByte(String roomTypeId) {
        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        return roomTypeImageRepo.getRoomTypeImagesByRoomTypeIdAndImageByteNotNull(roomTypeId);
    }
}
