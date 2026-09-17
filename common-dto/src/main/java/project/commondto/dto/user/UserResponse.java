package project.commondto.dto.user;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private Instant verifiedDate;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Instant dateOfBirth;
    private Gender gender;
    private String nationality;
    private String address;
    private String city;
    private String country;
    private List<String> roles;
}