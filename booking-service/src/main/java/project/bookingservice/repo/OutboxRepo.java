package project.bookingservice.repo;

import org.springframework.stereotype.Repository;
import project.bookingservice.entity.Outbox;
import project.commonutils.repo.OutboxRepoCM;

@Repository
public interface OutboxRepo extends OutboxRepoCM<Outbox> {
}
