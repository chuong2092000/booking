package project.commondto.dto.payment;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PaymentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String bookingId;
    private String transactionCode;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentGateway paymentGateway;
    private PaymentStatus paymentStatus;
    private BigDecimal refundedAmount;
    private String failureReason;
    private String rawGatewayResponse;
    private Instant paidAt;
    private Instant expireDate;
    private Instant refundedAt;
    private String paymentUrl;
    private Instant createdDate;
    private String ipAddress;
    private String ownerHotelId;
    private String keycloakId;
}
