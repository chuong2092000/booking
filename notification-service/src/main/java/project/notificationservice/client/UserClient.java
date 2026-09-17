package project.notificationservice.client;

import project.commondto.dto.user.UserResponse;

public interface UserClient {
    UserResponse getUserById(String id);
}
