package project.commondto.dto.payment;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class RefundPaymentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String transactionCode;
    private String bookingId;
    private BigDecimal refundAmount;
    private String reason;
}