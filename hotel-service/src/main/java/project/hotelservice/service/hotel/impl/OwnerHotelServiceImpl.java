package project.hotelservice.service.hotel.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.dto.request.CreateHotelDto;
import project.commondto.dto.hotel.owner.OwnerFullHotelDto;
import project.commondto.dto.hotel.owner.OwnerHotelDto;
import project.commondto.dto.hotel.owner.OwnerHotelImageDto;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.mapper.HotelImageMapper;
import project.hotelservice.mapper.HotelMapper;
import project.hotelservice.repo.HotelRepo;
import project.hotelservice.service.core.CoreHotelImageService;
import project.hotelservice.service.core.CoreHotelService;
import project.hotelservice.service.hotel.OwnerHotelService;

import java.util.List;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class OwnerHotelServiceImpl implements OwnerHotelService {


    private final CoreHotelService coreHotelService;

    private final CoreHotelImageService coreHotelImageService;

    private final HotelMapper hotelMapper;

    private final HotelImageMapper hotelImageMapper;

    private final HotelRepo hotelRepo;

    @Transactional
    @Override
    public OwnerHotelDto createHotel(CreateHotelDto createHotelDto, String ownerHotelId, MultipartFile mainImageFile, MultipartFile[] supportImageFiles) {
        return hotelMapper.toOwnerDto(
                coreHotelService.createHotel(createHotelDto, ownerHotelId, mainImageFile, supportImageFiles));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OwnerHotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable, String ownerHotelId, boolean isApproval) {
        return coreHotelService.getHotels(searchHotelDto, pageable, ownerHotelId, isApproval)
                .map(hotelMapper::toOwnerDto);
    }

    @Transactional(readOnly = true)
    @Override
    public OwnerFullHotelDto getHotel(String ownerHotelId, String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        Optional<Hotel> opHotel = hotelRepo.getHotelByIdAndKeycloakIdAndIsDeleted(hotelId, ownerHotelId, false);

        Hotel hotel = coreHotelService.getHotelFromOption(hotelId, opHotel);

        List<OwnerHotelImageDto> ownerHotelImagesDto = coreHotelImageService.getHotelImagesByHoTelId(hotel.getId()).stream()
                .map(hotelImageMapper::toOwnerDto).toList();

        OwnerFullHotelDto ownerFullHotelDto = hotelMapper.toOwnerFullDto(hotel);
        ownerFullHotelDto.setImages(ownerHotelImagesDto);

        return ownerFullHotelDto;
    }
}