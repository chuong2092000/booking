package project.commondto.dto.hotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.io.Serial;
import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class CreateHotelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Address is required")
    private String address;
    @NotBlank(message = "Description is required")
    private String description;
}