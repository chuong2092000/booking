package project.userservice.service.impl;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import project.commondto.dto.user.GroupRoles;
import project.commondto.dto.user.UserResponse;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.userservice.dto.CreateUserRequest;
import project.userservice.mapper.UserMapper;
import project.userservice.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class KeycloakUserImpl implements UserService {

    private final Keycloak keycloak;

    @Value(value = "${KC_REALM:hotel-realm}")
    private String realm;

    private final UserMapper userMapper;

    @Override
    public String createUser(CreateUserRequest request, List<GroupRoles> groups) {

        if (groups == null || request == null) {
            throw new BusinessException("Request and group roles cannot be null");
        }

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(request.getPassword().trim());

        String email = request.getEmail().trim();
        String userName = email.substring(0, email.indexOf('@')).trim();

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phone", List.of(request.getPhone()));

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userName);
        userRepresentation.setEmail(email);
        userRepresentation.setFirstName(request.getFirstName().trim());
        userRepresentation.setLastName(request.getLastName().trim());
        userRepresentation.setAttributes(attributes);
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setGroups(groups.stream().map(groupRoles -> "/" + groupRoles.name()).toList());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);

        Response response = keycloak.realm(realm).users().create(userRepresentation);

        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new BusinessException("Create user failed", response.getStatus());
        }

        String path = response.getLocation().getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);

        try {
            UserResource userResource = keycloak.realm(realm)
                    .users()
                    .get(keycloakUserId);
            userResource.sendVerifyEmail();
            return keycloakUserId;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        } finally {
            response.close();
        }
    }

    @Override
    public List<UserResponse> getUsers() {
        return List.of();
    }

    @Override
    public void assignGroupRolesToUser() {

    }

    @Override
    public UserResponse getUserById(String userId) {
        BaseUtils.validateObject(userId, "User id", false);
        try {
            return userMapper.toRes(keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .toRepresentation());
        } catch (NotFoundException e) {
            throw new BusinessException("User not found with id: " + userId);
        }
    }

}
