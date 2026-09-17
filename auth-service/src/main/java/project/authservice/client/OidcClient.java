package project.authservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import project.authservice.dto.AuthRequest;

public interface OidcClient {
    JsonNode getToken(AuthRequest request);

    JsonNode refreshToken(String refreshToken);

    JsonNode getUserInfo();

    void logout(String refreshToken);

    void revokeToken(String refreshToken);
}