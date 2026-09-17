package project.paymentservice.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.commonutils.core.OutBoxServiceCM;
import project.paymentservice.entity.Outbox;
import project.paymentservice.mapper.OutboxMapper;
import project.paymentservice.repo.OutboxRepo;


@Service
@Slf4j
public class OutboxService extends OutBoxServiceCM<Outbox> {
    public OutboxService(@Autowired OutboxRepo outboxRepo,
                         @Autowired OutboxMapper outboxMapper) {
        super(outboxRepo, outboxMapper);
    }
}