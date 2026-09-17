package project.embeddingservice.client;


import project.embeddingservice.client.dto.hotel.HotelDto;
import project.embeddingservice.client.dto.hotel.RoomTypeDto;
import project.embeddingservice.client.dto.hotel.SearchHotelDto;
import project.embeddingservice.client.dto.hotel.SearchRoomTypesDto;

import java.util.List;

public interface HotelClient {
    List<HotelDto> getHotels(SearchHotelDto searchHotelDto, int page, int limit);
    List<RoomTypeDto> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, int page, int limit);
}
