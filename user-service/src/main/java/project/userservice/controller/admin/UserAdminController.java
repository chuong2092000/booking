package project.userservice.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.AuthConstants;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.userservice.dto.CreateUserRequest;
import project.userservice.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.USERS + CommonUri.ADMIN)
public class UserAdminController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + AuthConstants.ADMIN_WRITE_USER
            + "','" + AuthConstants.ADMIN_SUPER + "')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        String userId = userService.createUser(request, request.getGroups());
        return BaseUtils.dataResponse("Create user success", userId, HttpStatus.CREATED);
    }


}
