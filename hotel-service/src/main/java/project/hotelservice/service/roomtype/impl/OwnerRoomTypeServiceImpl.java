package project.hotelservice.service.roomtype.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.dto.request.CreateRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerFullRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeDto;
import project.commondto.dto.hotel.owner.OwnerRoomTypeImageDto;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.entity.RoomType;
import project.hotelservice.mapper.RoomInventoryMapper;
import project.hotelservice.mapper.RoomTypeImageMapper;
import project.hotelservice.mapper.RoomTypeMapper;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.core.CoreHotelService;
import project.hotelservice.service.core.CoreRoomInventoryService;
import project.hotelservice.service.core.CoreRoomTypeImageService;
import project.hotelservice.service.core.CoreRoomTypeService;
import project.hotelservice.service.roomtype.OwnerRoomTypeService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OwnerRoomTypeServiceImpl implements OwnerRoomTypeService {

    private final CoreRoomTypeService coreRoomTypeService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    private final RoomTypeRepo roomTypeRepo;

    private final CoreHotelService coreHotelService;

    private final RoomTypeMapper roomTypeMapper;

    private final RoomTypeImageMapper roomTypeImageMapper;

    private final CoreRoomInventoryService coreRoomInventoryService;
    private final RoomInventoryMapper roomInventoryMapper;

    @Transactional
    @Override
    public OwnerRoomTypeDto createRoomType(String ownerHotelId, String hotelId, CreateRoomTypeDto createRoomTypeDto, MultipartFile mainImageFile, MultipartFile[] supportImageFiles) {
        return roomTypeMapper.toOwnerDto(
                coreRoomTypeService.createRoomType(ownerHotelId, hotelId, createRoomTypeDto, mainImageFile, supportImageFiles));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OwnerRoomTypeDto> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, Pageable pageable, String hotelId, String ownerHotelId, boolean isApproval) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);

        coreHotelService.existsHotelByOwnerHotelId(hotelId, ownerHotelId);

        return coreRoomTypeService.getRoomTypes(searchRoomTypesDto, pageable, hotelId, isApproval)
                .map(roomTypeMapper::toOwnerDto);
    }

    @Transactional(readOnly = true)
    @Override
    public OwnerFullRoomTypeDto getRoomType(String ownerHotelId, String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        BaseUtils.validateObject(ownerHotelId, "Owner hotel id", false);

        Optional<RoomType> opRoomType = roomTypeRepo.getRoomTypeByIdAndIsDeleted(roomTypeId, false);

        RoomType roomType = coreRoomTypeService.getRoomTypeFromOption(roomTypeId, opRoomType);

        coreHotelService.existsHotelByOwnerHotelId(roomType.getHotelId(), ownerHotelId);

        List<OwnerRoomTypeImageDto> roomTypeImages = coreRoomTypeImageService.getRoomTypeImagesByRoomTypeId(roomTypeId)
                .stream().map(roomTypeImageMapper::toOwnerDto).toList();

        List<RoomInventory> roomInventories = coreRoomInventoryService.getRoomInventoriesByRoomTypeId(roomTypeId, null, null, null);

        OwnerFullRoomTypeDto ownerFullRoomTypeDto = roomTypeMapper.toOwnerFullDto(roomType);
        ownerFullRoomTypeDto.setImages(roomTypeImages);
        ownerFullRoomTypeDto.setInventories(roomInventories.stream().map(roomInventoryMapper::toOwnerFullDto).toList());

        return ownerFullRoomTypeDto;
    }

}
