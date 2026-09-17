package project.commondto.dto.booking;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.custom.CheckOutAfterCheckIn;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class ReservedRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Room type id is required")
    private String roomTypeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Cannot book more than 10 rooms per request")
    private Integer quantity;
}