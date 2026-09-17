package project.apigateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import project.apigateway.dto.BaseResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthExceptionHandler implements ServerAccessDeniedHandler, ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return getVoidMono(exchange, status, ex.getMessage());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return getVoidMono(exchange, status, denied.getMessage());
    }

    private Mono<Void> getVoidMono(ServerWebExchange exchange, HttpStatus status, String message) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage(message);
        baseResponse.setStatus(status.value());

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.fromCallable(() -> {
            byte[] bytes = objectMapper.writeValueAsBytes(baseResponse);
            return response.bufferFactory().wrap(bytes);
        }));
    }
}
