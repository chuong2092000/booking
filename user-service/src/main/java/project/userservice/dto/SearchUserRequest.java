package project.userservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUserRequest {
    private String email;
    private boolean active;
    private boolean verified;
    private String fullName;
    private String phone;
}