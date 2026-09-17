package project.commondto.dto.hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class CreateRoomTypeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "From date is required")
    @FutureOrPresent(message = "From date must be today or in the future")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 9, fraction = 2, message = "Price must have at most 9 digits and 2 decimals")
    private BigDecimal price;

    @AssertTrue(message = "To date must be after from date")
    public boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return toDate.isAfter(fromDate);
    }
}