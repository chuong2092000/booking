package project.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import project.commondto.dto.user.GroupRoles;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Mật khẩu phải có ít nhất 1 chữ và 1 số"
    )
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50)
    private String lastName;

    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    private List<GroupRoles> groups;
}
