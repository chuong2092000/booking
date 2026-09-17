package project.commondto.dto.notification;

import lombok.*;
import project.commondto.dto.payment.PaymentMethod;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class MailPaymentRefunding {
    private String customerName;
    private String bookingId;
    private String transactionId;
    private BigDecimal fee;
    private PaymentMethod paymentMethod;
    private BigDecimal refundAmount;
    private BigDecimal paidAmount;
    private Integer refundDay;
    private String supportPhone;
    private String supportEmail;
    private String supportUrl;
}
