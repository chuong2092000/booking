package project.hotelservice.service.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.hotel.admin.AdminFullHotelDto;
import project.commondto.dto.hotel.admin.AdminHotelDto;
import project.hotelservice.service.IHotelService;

public interface AdminHotelService extends IHotelService {

    Page<AdminHotelDto> getHotels(SearchHotelDto searchHotelDto, Pageable pageable,
                                  String ownerHotelId, boolean isApproval);

    AdminFullHotelDto getHotel(String hotelId);

    AdminHotelDto approveHotel(String hotelId);

    void deleteHotel(String hotelId);
}
