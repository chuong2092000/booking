package project.authservice.controller.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.authservice.client.OidcClient;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.uri.AuthClientUri;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.AUTHS)
public class AuthAdminController {

    private final OidcClient OIDCClient;

    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_SUPER + "','" +
            AuthConstants.ADMIN_DELETE_AUTH + "')")
    @PostMapping(value = AuthClientUri.REVOKE_TOKEN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> revokeToken(@NotBlank(message = "Refresh token cannot be null")
                                         @RequestParam String refreshToken) {
        OIDCClient.revokeToken(refreshToken);
        return BaseUtils.baseResponse("Revoke token success", HttpStatus.NO_CONTENT);
    }
}
