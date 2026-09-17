package project.paymentservice.utils;

import project.commondto.dto.payment.vnpay.IpnRequest;
import project.commondto.dto.payment.vnpay.VnPayResponse;
import project.commonutils.BaseUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static project.commonutils.BaseUtils.*;

public class VnPayUtils {
    public static String vnp_TmnCode = "5YFS0MEE";
    public static String vnp_HashSecret = "3HR0EEGX6P2J8ALM3NMKP97NVCOC8B6B";
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "https://banister-glazing-recall.ngrok-free.dev/v1/public/payments/success";
    private static final String VERSION = "2.1.0";

    public static String createUrlPaymentVNPay(BigDecimal amountRaw, String transactionCode,
                                               String orderInfo, String clientIpAddr, String expireDate) {
        String createDate = getTimeStr(getZoneDateTimeNow());

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(toVnpAmount(amountRaw)));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transactionCode);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        params.put("vnp_IpAddr", clientIpAddr);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String query = buildQuery(params);
        String secureHash = hmacSHA512(vnp_HashSecret, query);
        return vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public static Map<String, Object> createParamsRefundPaymentVnPay(String transactionCode, Instant paidAt,
                                                                     BigDecimal refundAmount, String refundType,
                                                                     String createdBy, String ipAddress) {
        String createDate = getTimeStr(getZoneDateTimeNow());
        String requestId = UUID.randomUUID().toString();
        String txnDate = getTimeStr(getZoneDateTimeFromInstant(paidAt));
        long amount = toVnpAmount(refundAmount);
        String orderInfo = "Hoan tien don hang " + transactionCode;

        String hashData = String.join("|",
                requestId, VERSION, "refund", vnp_TmnCode, refundType,
                transactionCode, String.valueOf(amount), "", txnDate,
                createdBy, createDate, ipAddress, orderInfo);
        String secureHash = hmacSHA512(vnp_HashSecret, hashData);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", VERSION);
        body.put("vnp_Command", "refund");
        body.put("vnp_TmnCode", vnp_TmnCode);
        body.put("vnp_TransactionType", refundType);
        body.put("vnp_TxnRef", transactionCode);
        body.put("vnp_Amount", amount);
        body.put("vnp_TransactionNo", "");
        body.put("vnp_TransactionDate", txnDate);
        body.put("vnp_CreateBy", createdBy);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", "127.0.0.1");
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_SecureHash", secureHash);
        return body;
    }

    private static long toVnpAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private static String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            hmac512.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error create HMAC SHA512", ex);
        }
    }

    public static boolean verifyRefundResponseHash(VnPayResponse res) {
        String hashData = String.join("|",
                nvl(res.getVnp_ResponseId()),
                nvl(res.getVnp_Command()),
                nvl(res.getVnp_ResponseCode()),
                nvl(res.getVnp_Message()),
                nvl(res.getVnp_TmnCode()),
                nvl(res.getVnp_TxnRef()),
                nvl(res.getVnp_Amount()),
                nvl(res.getVnp_BankCode()),
                nvl(res.getVnp_PayDate()),
                nvl(res.getVnp_TransactionNo()),
                nvl(res.getVnp_TransactionType()),
                nvl(res.getVnp_TransactionStatus()),
                nvl(res.getVnp_OrderInfo())
        );

        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData);
        return calculatedHash.equalsIgnoreCase(res.getVnp_SecureHash());
    }

    public static boolean verifyIpnRequestHash(IpnRequest resp) {
        Map<String, String> fields = new TreeMap<>();
        putIfNotEmpty(fields, "vnp_TmnCode", resp.getVnp_TmnCode());
        putIfNotEmpty(fields, "vnp_Amount", resp.getVnp_Amount());
        putIfNotEmpty(fields, "vnp_BankCode", resp.getVnp_BankCode());
        putIfNotEmpty(fields, "vnp_BankTranNo", resp.getVnp_BankTranNo());
        putIfNotEmpty(fields, "vnp_CardType", resp.getVnp_CardType());
        putIfNotEmpty(fields, "vnp_PayDate", resp.getVnp_PayDate());
        putIfNotEmpty(fields, "vnp_OrderInfo", resp.getVnp_OrderInfo());
        putIfNotEmpty(fields, "vnp_TransactionNo", resp.getVnp_TransactionNo());
        putIfNotEmpty(fields, "vnp_ResponseCode", resp.getVnp_ResponseCode());
        putIfNotEmpty(fields, "vnp_TransactionStatus", resp.getVnp_TransactionStatus());
        putIfNotEmpty(fields, "vnp_TxnRef", resp.getVnp_TxnRef());


        String hashData = buildHashData(fields);
        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData);
        return calculatedHash.equalsIgnoreCase(resp.getVnp_SecureHash());
    }

    private static void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }

    private static String buildHashData(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append(urlEncode(entry.getKey()))
                    .append('=')
                    .append(urlEncode(entry.getValue()))
                    .append('&');
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
