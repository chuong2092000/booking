package project.commondto.dto.hotel.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateImageDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Uri image is required")
    private String uri;
    @NotBlank(message = "Name image is required")
    private String name;
    private byte[] file;
}