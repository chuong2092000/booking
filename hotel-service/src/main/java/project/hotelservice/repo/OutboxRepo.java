package project.hotelservice.repo;

import org.springframework.stereotype.Repository;

import project.commonutils.repo.OutboxRepoCM;
import project.hotelservice.entity.Outbox;

@Repository
public interface OutboxRepo extends OutboxRepoCM<Outbox> {
}
