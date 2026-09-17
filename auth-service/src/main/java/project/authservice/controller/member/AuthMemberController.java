package project.authservice.controller.member;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class AuthMemberController {

    private final OidcClient OIDCClient;

    @GetMapping(AuthClientUri.USER_INFO)
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_SUPER + "','" +
            AuthConstants.ADMIN_READ_AUTH + "'," +
            "'" + AuthConstants.USER_READ_AUTH + "'," +
            "'" + AuthConstants.OWNER_READ_AUTH + "')")
    public ResponseEntity<?> userInfo() {
        JsonNode userInfo = OIDCClient.getUserInfo();
        return BaseUtils.dataResponse("Get user info success", userInfo, HttpStatus.OK);
    }
}
