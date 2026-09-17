package project.bookingservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.bookingservice.entity.ProcessedEvent;
import project.bookingservice.mapper.ProcessEventMapper;
import project.bookingservice.repo.ProcessedEventRepo;
import project.commonutils.core.ProcessEventServiceCM;

@Service
@Slf4j
public class ProcessedEventService extends ProcessEventServiceCM<ProcessedEvent> {
    public ProcessedEventService(@Autowired ProcessedEventRepo processedEventRepo,
                                 @Autowired ProcessEventMapper processEventMapper) {
        super(processedEventRepo, processEventMapper);
    }
}