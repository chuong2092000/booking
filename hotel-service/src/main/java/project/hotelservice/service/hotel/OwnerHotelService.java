package project.hotelservice.service.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.dto.request.CreateHotelDto;
import project.commondto.dto.hotel.owner.OwnerFullHotelDto;
import project.commondto.dto.hotel.owner.OwnerHotelDto;
import project.hotelservice.service.IHotelService;

public interface OwnerHotelService extends IHotelService {
    OwnerHotelDto createHotel(CreateHotelDto createHotelDto, String ownerHotelId, MultipartFile mainImageFile, MultipartFile[] supportImageFiles);

    Page<OwnerHotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable,
                                  String ownerHotelId, boolean isApproval);

    OwnerFullHotelDto getHotel(String ownerHotelId, String hotelId);
}