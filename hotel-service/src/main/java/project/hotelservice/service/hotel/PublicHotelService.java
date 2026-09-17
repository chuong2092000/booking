package project.hotelservice.service.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.hotel.dto.FullHotelDto;
import project.commondto.dto.hotel.dto.HotelDto;
import project.commondto.dto.hotel.SearchHotelDto;
import project.hotelservice.service.IHotelService;

public interface PublicHotelService extends IHotelService {
    Page<HotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable);

    FullHotelDto getHotel(String hotelId);
}