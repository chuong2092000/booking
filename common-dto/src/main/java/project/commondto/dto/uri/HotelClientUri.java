package project.commondto.dto.uri;

public interface HotelClientUri {
    String GET_FULL_HOTEL_INTERNAL_BY_ROOM_TYPE_IDS = "/full/ids";
    String GET_ROOM_TYPES = "/room-types";
    String GET_OUTBOXES = "/out-boxes";
    String UPDATE_STATUS_OUTBOXES = "/out-boxes/status";

    String GET_INTERNAL_ROOM_TYPE = "/room-type/ids";

    String INR = "/increase";

    String DER = "/decrease";
}
