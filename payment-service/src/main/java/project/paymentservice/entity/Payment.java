package project.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.payment.PaymentGateway;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;
import project.commonutils.entity.core.CommonEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment extends CommonEntity {

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", length = 30)
    private PaymentGateway paymentGateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "refunded_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "raw_gateway_response", columnDefinition = "TEXT")
    private String rawGatewayResponse;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Builder.Default
    @Column(name = "is_confirmed")
    private boolean isConfirmed = false; // xử lý IPN r

    @Column(name = "expire_date")
    private Instant expireDate;

    @Column(name = "ip")
    private String ipAddress;

    @Column(name = "owner_hotel_id", nullable = false)
    private String ownerHotelId;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;
}
