package project.commondto.dto.payment;

public enum PaymentStatus {
    UNPAID, // chua thanh toan
    PAID, // da thanh toan
    PROCESSING_PAID, // dang thanh toan
    REFUNDED, // da hoan tien
    PROCESSING_REFUND, // dang xu ly hoan tien
    REFUND_FAILED,
    PAID_FAILED
}