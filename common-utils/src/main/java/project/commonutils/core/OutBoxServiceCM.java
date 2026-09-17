package project.commonutils.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.commonutils.entity.core.OutBoxCM;
import project.commonutils.mapper.OutboxMapperCM;
import project.commonutils.repo.OutboxRepoCM;
import project.commonutils.spec.OutboxSpec;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class OutBoxServiceCM<T extends OutBoxCM> {
    private final OutboxRepoCM<T> outboxRepo;
    private final OutboxMapperCM<T> outboxMapper;

    public OutBoxServiceCM(OutboxRepoCM<T> outboxRepo, OutboxMapperCM<T> outboxMapper) {
        this.outboxRepo = outboxRepo;
        this.outboxMapper = outboxMapper;
    }

    @Transactional
    public void createOutbox(OutBoxDto outBoxDto) {

        BaseUtils.validateObject(outBoxDto, "Outbox DTO", false);
        BaseUtils.validateObject(outBoxDto.getPayload(), "Payload outbox DTO", false);
        BaseUtils.validateObject(outBoxDto.getTopic(), "Topic outbox DTO", false);

        T entity = outboxMapper.toEntity(outBoxDto);
        OutBoxDto outboxMapperDto = outboxMapper.toDto(outboxRepo.save(entity));
        log.info("Saving outbox success with id: {}", outboxMapperDto.getId());
    }

    @Transactional
    public void createOutboxes(List<OutBoxDto> outBoxesDto) {

        BaseUtils.validateListObject(outBoxesDto, "Outboxes DTO", false);

        for (OutBoxDto outBoxDto: outBoxesDto){
            BaseUtils.validateObject(outBoxDto.getPayload(), "Payload outbox DTO", false);
            BaseUtils.validateObject(outBoxDto.getTopic(), "Topic outbox DTO", false);
        }

        List<T> entities = outBoxesDto.stream().map(outboxMapper::toEntity).toList();
        List<OutBoxDto> outboxMappersDto = outboxRepo.saveAll(entities).stream().map(outboxMapper::toDto).toList();
        log.info("Saving outbox success with id: {}", outboxMappersDto.stream().map(OutBoxDto::getId).collect(Collectors.joining(",")));
    }

    @Transactional
    public void updateSendOutbox(UpdateOutboxDto request) {

        BaseUtils.validateObject(request, "Request update status out box", false);
        BaseUtils.validateObject(request.getIds(), "Ids out box", false);

        int updatedCount = outboxRepo.updateSentStatus(request.getIds(), request.isSent());

        if (updatedCount != request.getIds().size()) {
            log.warn("Not found or could not be updated with ids: {}", request.getIds());
            throw new BusinessException("Some Ids were not found or could not be updated");
        }

        log.info("Updating outbox status sent ({}) success with ids: {}", request.isSent(), request.getIds());
    }

    @Transactional(readOnly = true)
    public List<OutBoxDto> getOutBoxes(Integer batchSize) {

        BaseUtils.validateObject(batchSize, "Batch size",false);

        Pageable pageable = PageRequest.of(0, batchSize);
        Specification<T> outSpec = OutboxSpec.<T>sortByCreateDate()
                .and(OutboxSpec.isDeleted(false))
                .and(OutboxSpec.isSent(false));
        log.info("Search outboxes with size: {}", batchSize);

        return outboxRepo.findAll(outSpec, pageable)
                .map(outboxMapper::toDto)
                .getContent();
    }
}
