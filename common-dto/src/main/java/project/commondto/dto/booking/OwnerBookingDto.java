package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class OwnerBookingDto extends BookingDto{
    private String paymentId;
    private String lastReason;
}
