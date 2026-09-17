package project.commondto.dto.hotel;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class SearchRoomTypesDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    @NotNull(message = "From date is required")
    @FutureOrPresent(message = "From date must be today or in the future")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    @FutureOrPresent(message = "To date must not be in the past")
    private LocalDate toDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @AssertTrue(message = "To date must be after from date")
    public boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return toDate.isAfter(fromDate);
    }
}
