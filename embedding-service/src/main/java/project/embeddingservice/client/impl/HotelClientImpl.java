package project.embeddingservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import project.embeddingservice.client.HotelClient;
import project.embeddingservice.client.dto.DataResponse;
import project.embeddingservice.client.dto.PageResponseDto;
import project.embeddingservice.client.dto.hotel.HotelDto;
import project.embeddingservice.client.dto.hotel.RoomTypeDto;
import project.embeddingservice.client.dto.hotel.SearchHotelDto;
import project.embeddingservice.client.dto.hotel.SearchRoomTypesDto;
import project.embeddingservice.client.dto.uri.CommonUri;
import project.embeddingservice.client.dto.uri.HotelClientUri;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotelClientImpl implements HotelClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    public List<HotelDto> getHotels(SearchHotelDto searchHotelDto, int page, int limit) {

        String fullUriWithParams = UriComponentsBuilder.fromUriString("lb://hotel-service" +
                        CommonUri.VERSION +
                        CommonUri.PUBLIC +
                        CommonUri.HOTELS)
                .queryParam("page", page)
                .queryParam("limit", limit)
//                .queryParam("name", "")
                .build().toUriString();

        DataResponse<PageResponseDto<HotelDto>> response = webClientBuilder.build()
                .get()
                .uri(fullUriWithParams)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataResponse<PageResponseDto<HotelDto>>>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.getData() == null) {
            throw new RuntimeException("Cannot get hotels");
        }
        return response.getData().getContent();
    }

    @Override
    public List<RoomTypeDto> getRoomTypes(SearchRoomTypesDto searchRoomTypesDto, int page, int limit) {
        String fullUriWithParams = UriComponentsBuilder.fromUriString("lb://hotel-service" +
                        CommonUri.VERSION +
                        CommonUri.PUBLIC +
                        CommonUri.HOTELS + HotelClientUri.GET_ROOM_TYPES)
                .queryParam("page", page)
                .queryParam("limit", limit)
//                .queryParam("name", searchRoomTypesDto.getName())
                .queryParam("hotelId", searchRoomTypesDto.getHotelId())
//                .queryParam("minPrice", searchRoomTypesDto.getMinPrice())
//                .queryParam("maxPrice", searchRoomTypesDto.getMaxPrice())
//                .queryParam("from", searchRoomTypesDto.getFrom())
//                .queryParam("to", searchRoomTypesDto.getTo())
//                .queryParam("availableQuantity", searchRoomTypesDto.getAvailableQuantity())
                .build().toUriString();

        DataResponse<PageResponseDto<RoomTypeDto>> response = webClientBuilder.build()
                .get()
                .uri(fullUriWithParams)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataResponse<PageResponseDto<RoomTypeDto>>>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.getData() == null) {
            throw new RuntimeException("Cannot get room-types");
        }
        log.info("Size {}",response.getData().getSize());
        return response.getData().getContent();
    }
}
