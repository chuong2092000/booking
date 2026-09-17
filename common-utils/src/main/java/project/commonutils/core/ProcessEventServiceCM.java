package project.commonutils.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.commondto.dto.ProcessedEventDto;
import project.commonutils.BaseUtils;
import project.commonutils.entity.core.ProcessedEventCM;
import project.commonutils.mapper.ProcessEventMapperCM;
import project.commonutils.repo.ProcessedEventRepoCM;

@Slf4j
public abstract class ProcessEventServiceCM<T extends ProcessedEventCM> {
    private final ProcessedEventRepoCM<T> processedEventRepo;
    private final ProcessEventMapperCM<T> processEventMapper;

    public ProcessEventServiceCM(ProcessedEventRepoCM<T> processedEventRepo, ProcessEventMapperCM<T> processEventMapper) {
        this.processedEventRepo = processedEventRepo;
        this.processEventMapper = processEventMapper;
    }

    @Transactional
    public ProcessedEventDto createSuccessEvent(ProcessedEventDto processedEventDto) {
        return createEvent(processedEventDto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessedEventDto createFailedEvent(ProcessedEventDto processedEventDto) {
        return createEvent(processedEventDto);
    }

    @Transactional(readOnly = true)
    public boolean existEventByRefId(String refId) {
        BaseUtils.validateObject(refId, "Ref id",false);
        return processedEventRepo.existsByRefIdAndIsDeleted(refId, false);
    }

    private ProcessedEventDto createEvent(ProcessedEventDto processedEventDto) {

        BaseUtils.validateObject(processedEventDto, "Processed event DTO",false);
        BaseUtils.validateObject(processedEventDto.getRefId(), "Ref id",false);

        boolean isEventProcessed = processedEventRepo.existsByRefIdAndIsDeleted(processedEventDto.getRefId(), false);

        if (isEventProcessed) {
            log.warn("Event with refId {} already processed. Ignoring.", processedEventDto.getRefId());
            return null;
        }

        T entity = processEventMapper.toEntity(processedEventDto);
        return processEventMapper.toDto(processedEventRepo.saveAndFlush(entity));
    }
}
