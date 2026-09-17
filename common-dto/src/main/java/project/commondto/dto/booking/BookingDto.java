package project.commondto.dto.booking;

import lombok.*;
import lombok.experimental.SuperBuilder;
import project.commondto.dto.payment.PaymentGateway;
import project.commondto.dto.payment.PaymentMethod;
import project.commondto.dto.payment.PaymentStatus;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@ToString
public class BookingDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String hotelId;
    private String hotelName;
    private String code;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private PaymentGateway paymentGateway;
    private BigDecimal totalAmount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String ownerHotelId;
    private String keycloakId;
}