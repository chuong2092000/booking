package project.commondto.dto.payment.outbox;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class BookingCancelEventDto {
    private String bookingId;
    private String reason;
}
