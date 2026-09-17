package project.eventhotelsservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.commondto.dto.BaseResponse;
import project.commondto.dto.DataResponse;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.uri.HotelClientUri;
import project.commondto.exception.BusinessException;
import project.eventhotelsservice.client.HotelClient;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotelClientImpl implements HotelClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    public List<OutBoxDto> getOutBoxes(Integer batchSize) {
        DataResponse<List<OutBoxDto>> response = webClientBuilder.build()
                .get()
                .uri("lb://hotel-service" + CommonUri.VERSION +
                        CommonUri.INTERNAL + CommonUri.HOTELS +
                        HotelClientUri.GET_OUTBOXES + "/" + batchSize)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataResponse<List<OutBoxDto>>>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.getData() == null) {
            throw new BusinessException("Cannot get out-boxes");
        }

        return response.getData();
    }

    @Override
    public void updateStatusOutboxes(UpdateOutboxDto request) {
        BaseResponse response = webClientBuilder.build()
                .patch()
                .uri("lb://hotel-service" + CommonUri.VERSION +
                        CommonUri.INTERNAL + CommonUri.HOTELS +
                        HotelClientUri.UPDATE_STATUS_OUTBOXES)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BaseResponse>() {
                })
                .timeout(Duration.ofSeconds(5))
                .block();
        if (response == null) {
            throw new BusinessException("Cannot updated out-boxes");
        }
    }
}
