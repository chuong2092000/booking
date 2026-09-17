package project.notificationservice.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import project.commondto.dto.DataResponse;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.user.UserResponse;
import project.commondto.exception.BusinessException;
import project.notificationservice.client.UserClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClientImpl implements UserClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    @Cacheable(
            cacheNames = "user",
            key = "#id",
            condition = "#id != null", unless = "#result == null")
    public UserResponse getUserById(String id) {
        try {
            String fullUriWithParams = UriComponentsBuilder.fromUriString("lb://user-service" +
                            CommonUri.VERSION +
                            CommonUri.INTERNAL +
                            CommonUri.USERS + "/" + id)
                    .build().toUriString();
            DataResponse<UserResponse> response = webClientBuilder.build()
                    .get()
                    .uri(fullUriWithParams)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<DataResponse<UserResponse>>() {
                    })
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof TimeoutException))
                    .block();
            if (response == null || response.getData() == null) {
                log.warn("Received null response when getting user {}", id);
                return null;
            }
            return response.getData();
        } catch (Exception e) {
            log.error("Failed to get user {}", id, e);
            return null;
        }
    }
}
