package project.eventbookingservice.client;

import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;

import java.util.List;

public interface BookingClient {
    List<OutBoxDto> getOutBoxes(Integer batchSize);

    void updateSentOutbox(UpdateOutboxDto request);
}
