package project.authservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.authservice.client.OidcClient;
import project.authservice.dto.AuthRequest;
import project.commondto.dto.uri.AuthClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.AUTHS)
public class AuthPublicController {

    private final OidcClient OIDCClient;

    @PostMapping(value = AuthClientUri.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        JsonNode token = OIDCClient.getToken(request);
        return BaseUtils.dataResponse("Login success", token, HttpStatus.OK);
    }

    @PostMapping(value = AuthClientUri.LOGOUT,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> logout(@NotBlank(message = "Refresh token cannot be null")
                                    @RequestParam String refreshToken) {
        OIDCClient.logout(refreshToken);
        return BaseUtils.baseResponse("Logout success", HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = AuthClientUri.REFRESH_TOKEN,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> refreshToken(@NotBlank(message = "Refresh token cannot be null")
                                          @RequestParam String refreshToken) {
        JsonNode token = OIDCClient.refreshToken(refreshToken);
        return BaseUtils.dataResponse("Refresh token success", token, HttpStatus.OK);
    }
}