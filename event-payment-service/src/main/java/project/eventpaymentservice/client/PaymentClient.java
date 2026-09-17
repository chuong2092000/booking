package project.eventpaymentservice.client;

import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;

import java.util.List;

public interface PaymentClient {
    List<OutBoxDto> getOutBoxes(Integer batchSize);

    void updateStatusOutboxes(UpdateOutboxDto request);
}
