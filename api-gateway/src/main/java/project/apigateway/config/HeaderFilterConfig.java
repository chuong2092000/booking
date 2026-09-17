package project.apigateway.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.stream.Collectors;

@Component
public class HeaderFilterConfig implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    String roles = jwtAuth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    String userId = jwtAuth.getName();
                    String rawToken = jwtAuth.getToken().getTokenValue();

                    var requestBuilder = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Roles", roles)
                            .header("Authorization", "Bearer " + rawToken);

                    var mutatedRequest = requestBuilder.build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
