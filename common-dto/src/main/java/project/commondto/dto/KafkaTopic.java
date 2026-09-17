package project.commondto.dto;

public interface KafkaTopic {
    String BOOKING_CREATED = "booking-created";
    String BOOKING_CANCELED = "booking-canceled";
    String PAYMENT_PROCESSING_PAID = "payment-processing-paid";
    String PAYMENT_PAID = "payment-paid";
    String PAYMENT_PAID_FAILED = "payment-paid-failed";
    String ROOM_DECREASED = "room-decreased";
    String ROOM_INCREASED = "room-increased";
    String ROOM_PROCESSING_INCREASE = "room-processing-increase";
    String PAYMENT_PROCESSING_REFUND = "payment-processing-refund";
    String PAYMENT_REFUNDED = "payment-refunded";
    String PAYMENT_REFUNDED_FAILED = "payment-refunded-failed";
    String NOTIFICATION_PAYMENT_SUCCESS = "notification-payment-success";
    String NOTIFICATION_PAYMENT_FAILED = "notification-payment-failed";
    String NOTIFICATION_BOOKING_CANCELED = "notification-booking-canceled";
    String NOTIFICATION_BOOKING_CONFIRMED = "notification-booking-confirmed";
    String NOTIFICATION_PAYMENT_REFUNDING = "notification-payment-refunding";
    String NOTIFICATION_PAYMENT_REFUNDED = "notification-payment-refunded";
    String PAYMENT_CASH_UPDATE_PAID = "payment_cash_update_paid";
    String S3_IMAGES_HOTEL = "s3-images-hotel";
    String S3_IMAGES_ROOM_TYPE = "s3-images-room-type";
}