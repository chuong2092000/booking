package project.userservice.service;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import project.commondto.dto.user.GroupRoles;
import project.commondto.dto.user.UserResponse;
import project.userservice.dto.CreateUserRequest;

import java.util.List;

public interface UserService {
    String createUser(CreateUserRequest request, List<GroupRoles> groups);

    List<UserResponse> getUsers();

    void assignGroupRolesToUser();
    UserResponse getUserById(String userId);
}