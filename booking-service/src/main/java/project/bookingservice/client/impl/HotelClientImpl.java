package project.bookingservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import project.bookingservice.client.HotelClient;
import project.commondto.dto.DataResponse;
import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.ReservedRequest;
import project.commondto.dto.hotel.internal.InternalBookingRoomDto;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.uri.HotelClientUri;
import project.commondto.exception.BusinessException;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotelClientImpl implements HotelClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    public List<InternalBookingRoomDto> getRoomTypeByIds(CreateBookingDto createBookingDto) {
        String fullUriWithParams = UriComponentsBuilder.fromUriString("lb://hotel-service" +
                        CommonUri.VERSION +
                        CommonUri.INTERNAL +
                        CommonUri.HOTELS +
                        HotelClientUri.GET_INTERNAL_ROOM_TYPE)
                .build().toUriString();
        DataResponse<List<InternalBookingRoomDto>> response = webClientBuilder.build()
                .post()
                .uri(fullUriWithParams)
                .bodyValue(createBookingDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataResponse<List<InternalBookingRoomDto>>>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.getData() == null) {
            throw new BusinessException("Cannot get room-types");
        }

        return response.getData();
    }
}
