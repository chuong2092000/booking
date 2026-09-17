package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class AdminBookingDto extends BookingDto {
    private String ownerHotelId;
    private String paymentId;
    private String lastReason;
}
