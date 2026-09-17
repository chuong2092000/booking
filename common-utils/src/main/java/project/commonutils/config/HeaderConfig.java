package project.commonutils.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

public class HeaderConfig {
    static void addHeaders(Authentication authentication) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("Authorization", Collections.singletonList("Bearer " + authentication.getCredentials()));
        headers.put("X-User-Id", Collections.singletonList(authentication.getPrincipal() + ""));
        headers.put("X-User-Roles", Collections.singletonList(authentication.getAuthorities() + ""));
        new HttpHeaders(headers);
    }
}
