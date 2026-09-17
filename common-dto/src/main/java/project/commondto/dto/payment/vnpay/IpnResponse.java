package project.commondto.dto.payment.vnpay;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class IpnResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String RspCode;
    private String Message;

    public static IpnResponse success() {
        return new IpnResponse("00", "Confirm Success");
    }

    public static IpnResponse orderNotFound() {
        return new IpnResponse("01", "Order not found");
    }

    public static IpnResponse orderAlreadyConfirmed() {
        return new IpnResponse("02", "Order already confirmed");
    }

    public static IpnResponse invalidAmount() {
        return new IpnResponse("04", "Invalid amount");
    }

    public static IpnResponse invalidChecksum() {
        return new IpnResponse("97", "Invalid checksum");
    }

    public static IpnResponse unknownError() {
        return new IpnResponse("99", "Unknown error");
    }
}