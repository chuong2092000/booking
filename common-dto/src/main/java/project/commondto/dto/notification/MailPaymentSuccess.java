package project.commondto.dto.notification;

import lombok.*;
import project.commondto.dto.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class MailPaymentSuccess {
    private String bookingId;
    private String customerId;
    private String customerName;
    private String transactionCode;
    private PaymentMethod  paymentMethod;
    private Instant paidAt;
    private BigDecimal amount;
    private String supportPhone;
    private String supportEmail;
}
