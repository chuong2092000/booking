package project.commondto.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.custom.CheckOutAfterCheckIn;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
@CheckOutAfterCheckIn
public class CreateBookingDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
//
//    @NotBlank(message = "Hotel id is required")
//    private String hotelId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be a future date")
    private LocalDate checkOutDate;

    @NotEmpty(message = "At least one reserved item must be selected")
    private List<@Valid ReservedRequest> reservedRequests;
}