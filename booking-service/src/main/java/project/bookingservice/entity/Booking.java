package project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.booking.BookingStatus;
import project.commondto.dto.payment.PaymentGateway;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;
import project.commonutils.entity.core.CommonEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "booking")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Booking extends CommonEntity {

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;

    @Column(name = "hotel_name", nullable = false)
    private String hotelName;

    @Column(name = "hotel_image_url", nullable = false)
    private String hotelImageUrl;

    @Column(name = "owner_hotel_id", nullable = false)
    private String ownerHotelId;

    @Column(name = "booking_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "last_reason")
    private String lastReason;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_gateway")
    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "real_check_in_date", nullable = false)
    private LocalDate realCheckInDate;

    @Column(name = "real_check_out_date", nullable = false)
    private LocalDate realCheckOutDate;
}
