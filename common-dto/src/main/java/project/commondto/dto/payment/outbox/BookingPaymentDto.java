package project.commondto.dto.payment.outbox;

import lombok.*;
import project.commondto.dto.payment.PaymentGateway;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class BookingPaymentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String bookingId;
    private String paymentId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private PaymentGateway paymentGateway;
    private Instant createdDate;
    private Instant expireDate;
}