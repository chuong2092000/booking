package project.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    @Bean("ipKeyResolver")
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Objects.requireNonNull(exchange
                    .getRequest()
                    .getRemoteAddress()).getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
}