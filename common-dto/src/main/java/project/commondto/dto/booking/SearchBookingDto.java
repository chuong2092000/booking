package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.payment.PaymentStatus;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class SearchBookingDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private String hotelId;
}
