package project.hotelservice.service.hotel.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.dto.FullHotelDto;
import project.commondto.dto.hotel.dto.HotelDto;
import project.commondto.dto.hotel.dto.HotelImageDto;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.Hotel;
import project.hotelservice.mapper.HotelImageMapper;
import project.hotelservice.mapper.HotelMapper;
import project.hotelservice.repo.HotelRepo;
import project.hotelservice.service.core.CoreHotelImageService;
import project.hotelservice.service.core.CoreHotelService;
import project.hotelservice.service.hotel.PublicHotelService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PublicHotelServiceImpl implements PublicHotelService {

    private final CoreHotelService coreHotelService;

    private final CoreHotelImageService coreHotelImageService;

    private final HotelMapper hotelMapper;

    private final HotelImageMapper hotelImageMapper;

    private final HotelRepo hotelRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<HotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable) {
        return coreHotelService.getHotels(searchHotelDto, pageable, null, true)
                .map(hotelMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public FullHotelDto getHotel(String hotelId) {

        BaseUtils.validateObject(hotelId, "Hotel id", false);

        Optional<Hotel> opHotel = hotelRepo.getHotelByIdAndIsDeletedAndIsApproval(hotelId, false,true);

        Hotel hotel = coreHotelService.getHotelFromOption(hotelId, opHotel);

        List<HotelImageDto> hotelImagesDto = coreHotelImageService.getHotelImagesByHoTelId(hotel.getId()).stream()
                .map(hotelImageMapper::toDto).toList();

        FullHotelDto fullHotelDto = hotelMapper.toFullDto(hotel);
        fullHotelDto.setImages(hotelImagesDto);

        return fullHotelDto;
    }
}