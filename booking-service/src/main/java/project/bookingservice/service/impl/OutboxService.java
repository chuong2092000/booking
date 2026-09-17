package project.bookingservice.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.bookingservice.entity.Outbox;
import project.bookingservice.mapper.OutboxMapper;
import project.bookingservice.repo.OutboxRepo;
import project.commonutils.core.OutBoxServiceCM;


@Service
@Slf4j
public class OutboxService extends OutBoxServiceCM<Outbox> {
    public OutboxService(@Autowired OutboxRepo outboxRepo,
                         @Autowired OutboxMapper outboxMapper) {
        super(outboxRepo, outboxMapper);
    }
}