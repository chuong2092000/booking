package project.userservice.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.user.UserResponse;
import project.commonutils.BaseUtils;
import project.userservice.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.INTERNAL + CommonUri.USERS)
public class InternalController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return BaseUtils.dataResponse("Get user message", user, HttpStatus.OK);
    }
}
