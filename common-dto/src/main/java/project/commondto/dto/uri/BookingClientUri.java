package project.commondto.dto.uri;

public interface BookingClientUri {
    String GET_OUTBOXES = "/out-boxes";

    String UPDATE_STATUS_OUTBOXES = "/out-boxes/status";

    String GET_BOOKING_DETAILS = "/details";

    String CANCEL_BOOKING = "/cancel";

    String CONFIRM_BOOKING = "/confirm";

    String STATUS = "/status";

    String GET_INVENTORIES = "/inventories";
}
