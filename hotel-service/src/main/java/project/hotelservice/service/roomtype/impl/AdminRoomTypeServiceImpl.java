package project.hotelservice.service.roomtype.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.hotel.SearchRoomTypesDto;
import project.commondto.dto.hotel.admin.AdminFullRoomTypeDto;
import project.commondto.dto.hotel.admin.AdminRoomTypeDto;
import project.commondto.dto.hotel.admin.AdminRoomTypeImageDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.entity.RoomType;
import project.hotelservice.mapper.RoomInventoryMapper;
import project.hotelservice.mapper.RoomTypeImageMapper;
import project.hotelservice.mapper.RoomTypeMapper;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.core.CoreRoomInventoryService;
import project.hotelservice.service.core.CoreRoomTypeImageService;
import project.hotelservice.service.core.CoreRoomTypeService;
import project.hotelservice.service.roomtype.AdminRoomTypeService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminRoomTypeServiceImpl implements AdminRoomTypeService {

    private final CoreRoomTypeService coreRoomTypeService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    private final CoreRoomInventoryService coreRoomInventoryService;

    private final RoomTypeMapper roomTypeMapper;

    private final RoomTypeImageMapper roomTypeImageMapper;

    private final RoomTypeRepo roomTypeRepo;

    private final RoomInventoryMapper roomInventoryMapper;


    @Transactional(readOnly = true)
    @Override
    public Page<AdminRoomTypeDto> getRoomTypes(String name, Pageable pageable, String hotelId, boolean isApproval) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);
        SearchRoomTypesDto searchRoomTypesDto = SearchRoomTypesDto.builder()
                .fromDate(null)
                .toDate(null)
                .quantity(null)
                .build();
        return coreRoomTypeService.getRoomTypes(searchRoomTypesDto, pageable, hotelId, isApproval)
                .map(roomTypeMapper::toAdminDto);
    }

    @Transactional(readOnly = true)
    @Override
    public AdminFullRoomTypeDto getRoomType(String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);

        Optional<RoomType> opRoomType = roomTypeRepo.getRoomTypeByIdAndIsDeleted(roomTypeId, false);

        RoomType roomType = coreRoomTypeService.getRoomTypeFromOption(roomTypeId, opRoomType);

        List<AdminRoomTypeImageDto> roomTypeImages = coreRoomTypeImageService.getRoomTypeImagesByRoomTypeId(roomType.getId())
                .stream().map(roomTypeImageMapper::toAdminDto).toList();

        List<RoomInventory> roomInventories = coreRoomInventoryService.getRoomInventoriesByRoomTypeId(roomTypeId, null, null, null);

        AdminFullRoomTypeDto adminFullDto = roomTypeMapper.toAdminFullDto(roomType);
        adminFullDto.setImages(roomTypeImages);
        adminFullDto.setInventories(roomInventories.stream().map(roomInventoryMapper::toAdminFullDto).toList());

        return adminFullDto;
    }

    @Transactional
    @Override
    public AdminRoomTypeDto approveRoomType(String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);

        Optional<RoomType> opRoomType = roomTypeRepo.getRoomTypeByIdAndIsDeletedAndIsApproval(roomTypeId, false, false);

        RoomType roomType = coreRoomTypeService.getRoomTypeFromOption(roomTypeId, opRoomType);
        roomType.setApproval(true);

        return roomTypeMapper.toAdminDto(roomType);
    }

    @Transactional
    @Override
    public void deleteRoomType(String roomTypeId) {

        BaseUtils.validateObject(roomTypeId, "Room type id", false);

        if (roomTypeRepo.deleteRoomTypeById(roomTypeId) <= 0) {
            log.warn("Delete room type failed with id: {}", roomTypeId);
            throw new BusinessException("Room type cannot delete with id: " + roomTypeId);
        }

        coreRoomTypeImageService.deleteRoomTypeImageByRoomTypeId(roomTypeId);
        coreRoomInventoryService.deleteRoomInventoryByRoomTypeId(roomTypeId);

        log.info("Delete room type and related inventory success with id: {}", roomTypeId);
    }
}
