package project.paymentservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.commonutils.core.ProcessEventServiceCM;
import project.paymentservice.entity.ProcessedEvent;
import project.paymentservice.mapper.ProcessEventMapper;
import project.paymentservice.repo.ProcessedEventRepo;

@Service
@Slf4j
public class ProcessedEventService extends ProcessEventServiceCM<ProcessedEvent> {
    public ProcessedEventService(@Autowired ProcessedEventRepo processedEventRepo,
                                 @Autowired ProcessEventMapper processEventMapper) {
        super(processedEventRepo, processEventMapper);
    }
}