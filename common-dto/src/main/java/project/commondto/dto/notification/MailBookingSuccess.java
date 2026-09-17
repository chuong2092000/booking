package project.commondto.dto.notification;

import lombok.*;
import project.commondto.dto.hotel.internal.InternalFullRoomTypeDto;
import project.commondto.dto.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class MailBookingSuccess {
    private String bookingId;
    private String customerName;
    private String bookingCode;
    private String hotelName;
    private String checkInDate;
    private String checkOutDate;
    private PaymentStatus paymentStatus;
    private String supportPhone;
    private String supportEmail;
    private BigDecimal totalPrice;
    private List<InternalFullRoomTypeDto> rooms;
}
