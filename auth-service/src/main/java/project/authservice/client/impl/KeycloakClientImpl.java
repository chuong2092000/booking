package project.authservice.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import project.authservice.client.OidcClient;
import project.authservice.dto.AuthRequest;
import project.authservice.dto.GrantType;
import project.commondto.exception.BusinessException;
import project.authservice.exception.KeycloakException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class KeycloakClientImpl implements OidcClient {

    private final WebClient.Builder webClientBuilder;

    @Value(value = "${KC_CLIENT_ID:booking-client}")
    private String clientId;

    @Value(value = "${KC_CLIENT_SR:jonGcyG96q4QgkMV6oUq1Zva8gxoWYYT}")
    private String clientSecret;

    @Value(value = "${KC_REALM:hotel-realm}")
    private String realm;

    @Value(value = "${KC_HOST:http://localhost}" + ":${KC_PORT:8083}")
    private String hostPort;

    @Override
    public JsonNode getToken(AuthRequest request) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", GrantType.PASSWORD.name().toLowerCase());
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());
        formData.add("scope", "openid profile-user-booking-client");

        return execute(formData, "/token");
    }

    @Override
    public JsonNode refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", GrantType.REFRESH_TOKEN.name().toLowerCase());
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        formData.add("scope", "openid profile-user-booking-client");


        return execute(formData, "/token");
    }

    @Override
    public JsonNode getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new BusinessException("Token credentials invalid");
        }

        ResponseEntity<JsonNode> response = webClientBuilder.build().get()
                .uri(hostPort + "/realms/" + realm + "/protocol/openid-connect/userinfo")
                .header("Authorization", "Bearer " + authentication.getCredentials())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(KeycloakException.class)
                                .flatMap(errorBody -> Mono.error(new BusinessException(errorBody.getError() + ", " + errorBody.getErrorDescription())))
                )
                .toEntity(JsonNode.class)
                .block();

        assert response != null;
        return response.getBody();
    }

    @Override
    public void logout(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        execute(formData, "/logout");
    }

    @Override
    public void revokeToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("token", refreshToken);
        formData.add("token_type_hint", "refresh_token");

        execute(formData, "/revoke");
    }

    private JsonNode execute(MultiValueMap<String, String> body, String endPoint) {
        if (endPoint.isEmpty()) {
            throw new BusinessException("End point cannot be null");
        }

        ResponseEntity<JsonNode> response = webClientBuilder.build().post()
                .uri(hostPort + "/realms/" + realm + "/protocol/openid-connect" + endPoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(KeycloakException.class)
                                .flatMap(errorBody -> Mono.error(new BusinessException(errorBody.getError() + ", " + errorBody.getErrorDescription())))
                )
                .toEntity(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        assert response != null;
        return response.getBody();
    }
}
