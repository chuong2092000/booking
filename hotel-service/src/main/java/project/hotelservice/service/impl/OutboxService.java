package project.hotelservice.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.commonutils.core.OutBoxServiceCM;
import project.hotelservice.entity.Outbox;
import project.hotelservice.mapper.OutboxMapper;
import project.hotelservice.repo.OutboxRepo;


@Service
@Slf4j
public class OutboxService extends OutBoxServiceCM<Outbox> {
    public OutboxService(@Autowired OutboxRepo outboxRepo,
                         @Autowired OutboxMapper outboxMapper) {
        super(outboxRepo, outboxMapper);
    }
}