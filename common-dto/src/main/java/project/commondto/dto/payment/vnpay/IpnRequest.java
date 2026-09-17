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
public class IpnRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String vnp_TmnCode;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_BankTranNo;
    private String vnp_CardType;
    private String vnp_PayDate;
    private String vnp_OrderInfo;
    private String vnp_TransactionNo;
    private String vnp_ResponseCode;
    private String vnp_TransactionStatus;
    private String vnp_TxnRef;
    private String vnp_SecureHashType;
    private String vnp_SecureHash;
}
