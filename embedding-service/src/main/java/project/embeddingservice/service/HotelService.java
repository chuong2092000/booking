package project.embeddingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import project.embeddingservice.client.HotelClient;
import project.embeddingservice.client.dto.hotel.HotelDto;
import project.embeddingservice.client.dto.hotel.RoomTypeDto;
import project.embeddingservice.client.dto.hotel.SearchHotelDto;
import project.embeddingservice.client.dto.hotel.SearchRoomTypesDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelClient hotelClient;

    @Tool(description = "Search hotels")
    public List<HotelDto> getHotels(
            @ToolParam(description = "The page number for pagination. Usually starts at 0 (for the first page) unless specified by the user.") int page,
            @ToolParam(description = "The maximum number of hotels to return per page. If the user doesn't specify, default to 10.") int limit
    ) {
        SearchHotelDto searchHotelDto = SearchHotelDto.builder()
                .keycloakId(null)
                .name(null)
                .build();
        List<HotelDto> hotels = hotelClient.getHotels(searchHotelDto, page, limit);
        return hotels;
    }

    @Tool(description = "Search list room-types of hotel")
    public List<RoomTypeDto> getRoomTypes(
            @ToolParam(description = "The page number for pagination. Usually starts at 0 (for the first page) unless specified by the user.") int page,
            @ToolParam(description = "The maximum number of room-types to return per page. If the user doesn't specify, default to 10.") int limit,
            @ToolParam(description = "Id of hotel") String hotelId
    ) {
        SearchRoomTypesDto searchRoomTypesDto = SearchRoomTypesDto.builder()
                .hotelId(hotelId)
                .build();
        List<RoomTypeDto> roomTypesDto = hotelClient.getRoomTypes(searchRoomTypesDto, page, limit);
        return roomTypesDto;
    }
}
