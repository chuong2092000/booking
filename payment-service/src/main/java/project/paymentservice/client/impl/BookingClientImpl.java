package project.paymentservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import project.commondto.dto.DataResponse;
import project.commondto.dto.booking.BookingDto;
import project.commondto.dto.uri.CommonUri;
import project.commondto.exception.BusinessException;
import project.paymentservice.client.BookingClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingClientImpl implements BookingClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    public List<BookingDto> getBookingsHaveBookingInventoriesLockedByBookingIds(List<String> bookingIds) {
        String fullUriWithParams = UriComponentsBuilder.fromUriString("lb://booking-service" +
                        CommonUri.VERSION +
                        CommonUri.INTERNAL +
                        CommonUri.BOOKINGS + "/ids")
                .build().toUriString();
        DataResponse<List<BookingDto>> response = webClientBuilder.build()
                .post()
                .uri(fullUriWithParams)
                .bodyValue(bookingIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataResponse<List<BookingDto>>>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.getData().isEmpty()) {
            throw new BusinessException("Cannot get bookings");
        }

        return response.getData();
    }
}
