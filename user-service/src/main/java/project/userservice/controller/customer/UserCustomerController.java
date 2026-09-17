package project.userservice.controller.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.uri.CommonUri;
import project.commondto.dto.user.RolesDefault;
import project.commonutils.BaseUtils;
import project.userservice.dto.CreateUserRequest;
import project.userservice.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.USERS + CommonUri.CUSTOMER)
public class UserCustomerController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateUserRequest request) {
        String userId = userService.createUser(request, RolesDefault.rolesUser);
        return BaseUtils.dataResponse("Create customer success", userId, HttpStatus.CREATED);
    }
}