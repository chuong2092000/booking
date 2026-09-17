package project.eventhotelsservice.client;

import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;

import java.util.List;

public interface HotelClient {
    List<OutBoxDto> getOutBoxes(Integer batchSize);

    void updateStatusOutboxes(UpdateOutboxDto request);
}
