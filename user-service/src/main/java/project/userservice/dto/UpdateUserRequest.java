package project.userservice.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import project.commondto.dto.user.Gender;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    private String avatarUrl;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private Instant dateOfBirth;

    private Gender gender;

    private String nationality;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;
}