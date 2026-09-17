package project.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import project.apigateway.exception.AuthExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, HeaderFilterConfig headerFilterConfig, AuthExceptionHandler authExceptionHandler) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("v1/public/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info", "/actuator/refresh").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtMonoConverter()))
                        .authenticationEntryPoint(authExceptionHandler)
                        .accessDeniedHandler(authExceptionHandler)
                )
                .exceptionHandling(exceptionHandler -> exceptionHandler
                        .authenticationEntryPoint(authExceptionHandler)
                        .accessDeniedHandler(authExceptionHandler)
                )
                .addFilterAfter(headerFilterConfig, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtMonoConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object objectRole = jwt.getClaim("roles");
            if (objectRole == null) return List.of();
            List<String> roles = (List<String>) objectRole;
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.toLowerCase()))
                    .collect(Collectors.toSet());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

}