package project.userservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class KeycloakConfig {

    @Value(value = "${KC_CLIENT_ID:booking-client}")
    private String clientId;

    @Value(value = "${KC_CLIENT_SR:jonGcyG96q4QgkMV6oUq1Zva8gxoWYYT}")
    private String clientSecret;

    @Value(value = "${KC_REALM:hotel-realm}")
    private String realm;

    @Value(value = "${KC_HOST:http://localhost}" + ":${KC_PORT:8083}")
    private String hostPort;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(hostPort)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
