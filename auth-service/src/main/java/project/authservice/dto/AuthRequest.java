package project.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank(message = "Username cannot be null")
    private String username;

    @NotBlank(message = "Password cannot be null")
    @Size(min = 8, message = "Password is least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password is least 1 character and 1 number"
    )
    private String password;
}
