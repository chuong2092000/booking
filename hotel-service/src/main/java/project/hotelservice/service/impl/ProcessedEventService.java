package project.hotelservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.commonutils.core.ProcessEventServiceCM;
import project.hotelservice.entity.ProcessedEvent;
import project.hotelservice.mapper.ProcessEventMapper;
import project.hotelservice.repo.ProcessedEventRepo;

@Service
@Slf4j
public class ProcessedEventService extends ProcessEventServiceCM<ProcessedEvent> {
    public ProcessedEventService(@Autowired ProcessedEventRepo processedEventRepo,
                                 @Autowired ProcessEventMapper processEventMapper) {
        super(processedEventRepo, processEventMapper);
    }
}
