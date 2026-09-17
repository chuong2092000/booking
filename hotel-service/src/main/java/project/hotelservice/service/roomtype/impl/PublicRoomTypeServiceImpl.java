package project.hotelservice.service.roomtype.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.FullRoomTypeDto;
import project.commondto.dto.hotel.dto.RoomTypeImageDto;
import project.commondto.dto.hotel.dto.SearchRoomTypeRes;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.entity.RoomType;
import project.hotelservice.mapper.RoomTypeImageMapper;
import project.hotelservice.mapper.RoomTypeMapper;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.core.CoreRoomInventoryService;
import project.hotelservice.service.core.CoreRoomTypeImageService;
import project.hotelservice.service.core.CoreRoomTypeService;
import project.hotelservice.service.roomtype.PublicRoomTypeService;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PublicRoomTypeServiceImpl implements PublicRoomTypeService {

    private final CoreRoomTypeService coreRoomTypeService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    private final RoomTypeMapper roomTypeMapper;

    private final RoomTypeImageMapper roomTypeImageMapper;

    private final RoomTypeRepo roomTypeRepo;
    private final CoreRoomInventoryService coreRoomInventoryService;

    @Transactional(readOnly = true)
    @Override
    public List<SearchRoomTypeRes> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        List<RoomType> roomTypes = coreRoomTypeService.getRoomTypes(searchRoomTypesDto, hotelId, true);

        if (roomTypes.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roomTypeIds = roomTypes.stream().map(RoomType::getId).toList();

        List<RoomInventory> roomInventories = coreRoomInventoryService
                .getRoomInventoriesByRoomTypeIds(roomTypeIds, searchRoomTypesDto.getFromDate(),
                        searchRoomTypesDto.getToDate(), searchRoomTypesDto.getQuantity());

        BigDecimal quantity = BigDecimal.valueOf(searchRoomTypesDto.getQuantity());
        Map<String, BigDecimal> mapPrice = new HashMap<>();

        roomInventories.forEach(r -> {
            BigDecimal priceForQuantity = r.getPrice().multiply(quantity);
            mapPrice.merge(r.getRoomTypeId(), priceForQuantity, BigDecimal::add);
        });

        return roomTypes.stream()
                .filter(s -> mapPrice.containsKey(s.getId()))
                .map(s -> {
                    SearchRoomTypeRes searchRoomTypeRes = roomTypeMapper.toSearchRoomTypeRes(s);
                    searchRoomTypeRes.setPrice(mapPrice.get(s.getId()));
                    searchRoomTypeRes.setAvailableQuantity(searchRoomTypesDto.getQuantity());
                    return searchRoomTypeRes;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public FullRoomTypeDto getRoomType(String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);

        Optional<RoomType> opRoomType = roomTypeRepo.getRoomTypeByIdAndIsDeletedAndIsApproval(roomTypeId, false, true);

        RoomType roomType = coreRoomTypeService.getRoomTypeFromOption(roomTypeId, opRoomType);

        List<RoomTypeImageDto> roomTypeImages = coreRoomTypeImageService.getRoomTypeImagesByRoomTypeId(roomType.getId())
                .stream().map(roomTypeImageMapper::toDto).toList();

        FullRoomTypeDto fullDto = roomTypeMapper.toFullDto(roomType);
        fullDto.setImages(roomTypeImages);

        return fullDto;
    }


}
