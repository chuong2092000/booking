package project.hotelservice.service.hotel.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.admin.AdminFullHotelDto;
import project.commondto.dto.hotel.admin.AdminHotelDto;
import project.commondto.dto.hotel.admin.AdminHotelImageDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.mapper.HotelImageMapper;
import project.hotelservice.mapper.HotelMapper;
import project.hotelservice.repo.HotelRepo;
import project.hotelservice.repo.RoomTypeRepo;
import project.hotelservice.service.core.*;
import project.hotelservice.service.hotel.AdminHotelService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminHotelServiceImpl implements AdminHotelService {

    private final CoreHotelService coreHotelService;

    private final CoreHotelImageService coreHotelImageService;

    private final HotelMapper hotelMapper;

    private final HotelImageMapper hotelImageMapper;

    private final HotelRepo hotelRepo;

    private final RoomTypeRepo roomTypeRepo;

    private final CoreRoomTypeService coreRoomTypeService;

    private final CoreRoomTypeImageService coreRoomTypeImageService;

    private final CoreRoomInventoryService coreRoomInventoryService;

    @Transactional(readOnly = true)
    @Override
    public Page<AdminHotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable, String ownerHotelId, boolean isApproval) {
        return coreHotelService.getHotels(searchHotelDto, pageable, ownerHotelId, isApproval)
                .map(hotelMapper::toAdminDto);
    }

    @Transactional(readOnly = true)
    @Override
    public AdminFullHotelDto getHotel(String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        Optional<Hotel> opHotel = hotelRepo.getHotelByIdAndIsDeleted(hotelId, false);

        Hotel hotel = coreHotelService.getHotelFromOption(hotelId, opHotel);

        List<AdminHotelImageDto> adminHotelImagesDto = coreHotelImageService.getHotelImagesByHoTelId(hotel.getId()).stream()
                .map(hotelImageMapper::toAdminDto).toList();

        AdminFullHotelDto adminFullHotelDto = hotelMapper.toAdminFullDto(hotel);
        adminFullHotelDto.setImages(adminHotelImagesDto);

        return adminFullHotelDto;
    }

    @Transactional
    @Override
    public AdminHotelDto approveHotel(String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        Optional<Hotel> opHotel = hotelRepo.getHotelByIdAndIsDeletedAndIsApproval(hotelId, false, false);

        Hotel hotel = coreHotelService.getHotelFromOption(hotelId, opHotel);
        hotel.setApproval(true);

        return hotelMapper.toAdminDto(hotel);
    }

    @Transactional
    @Override
    public void deleteHotel(String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        if (hotelRepo.deleteHotelById(hotelId) <= 0) {
            log.warn("Delete hotel failed with id: {}", hotelId);
            throw new BusinessException("Hotel cannot delete with id: " + hotelId);
        }

        coreHotelImageService.deleteHotelImageByHotelId(hotelId);

        List<String> roomTypeIds = roomTypeRepo.findIdsByHotelId(hotelId);

        if (!roomTypeIds.isEmpty()) {
            coreRoomTypeService.deleteRoomTypeByIds(roomTypeIds);
            coreRoomTypeImageService.deleteRoomTypeImageByRoomTypeIds(roomTypeIds);
            coreRoomInventoryService.deleteRoomInventoryByRoomTypeIds(roomTypeIds);
        }
        log.info("Delete Hotel and related room type success with id: {}", hotelId);
    }
}