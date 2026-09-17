package project.commonutils.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;
import project.commonutils.BaseUtils;
import project.commonutils.core.OutBoxServiceCM;
import project.commonutils.entity.core.OutBoxCM;

import java.util.List;

public abstract class OutboxControllerCM<T extends OutBoxCM> {

    private final OutBoxServiceCM<T> outBoxService;

    public OutboxControllerCM(OutBoxServiceCM<T> outBoxService) {
        this.outBoxService = outBoxService;
    }

    public ResponseEntity<?> updateStatusOutboxes(UpdateOutboxDto request) {
        outBoxService.updateSendOutbox(request);
        return BaseUtils.baseResponse("Update outboxes success", HttpStatus.OK);
    }

    public ResponseEntity<?> getOutBoxes(Integer batchSize) {
        return BaseUtils.dataResponse("Get outboxes success",
                outBoxService.getOutBoxes(batchSize), HttpStatus.OK);
    }
}