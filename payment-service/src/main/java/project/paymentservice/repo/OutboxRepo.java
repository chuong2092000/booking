package project.paymentservice.repo;

import org.springframework.stereotype.Repository;

import project.commonutils.repo.OutboxRepoCM;
import project.paymentservice.entity.Outbox;

@Repository
public interface OutboxRepo extends OutboxRepoCM<Outbox> {
}
