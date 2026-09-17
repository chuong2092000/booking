package project.embeddingservice.client.dto.uri;

public interface HotelClientUri {
    String GET_ROOM_TYPES_BY_IDS = "/room-types/ids";
    String GET_ROOM_TYPES = "/room-types";
    String GET_OUTBOXES = "/out-boxes";
    String UPDATE_STATUS_OUTBOXES = "/out-boxes/status";
}
