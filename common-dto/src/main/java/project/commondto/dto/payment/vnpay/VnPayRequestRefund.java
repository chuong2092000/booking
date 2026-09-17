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
public class VnPayRequestRefund implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String vnp_RequestId;
    private String vnp_Version;
    private String vnp_Command;
    private String vnp_TmnCode;
    private String vnp_TransactionType;
    private String vnp_TxnRef;
    private long vnp_Amount;
    private String vnp_OrderInfo;
    private String vnp_TransactionNo;
    private String vnp_TransactionDate;
    private String vnp_CreateBy;
    private String vnp_CreateDate;
    private String vnp_IpAddr;
    private String vnp_SecureHash;
}
