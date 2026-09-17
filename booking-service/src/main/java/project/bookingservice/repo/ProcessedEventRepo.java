package project.bookingservice.repo;

import org.springframework.stereotype.Repository;
import project.bookingservice.entity.ProcessedEvent;
import project.commonutils.repo.ProcessedEventRepoCM;

@Repository
public interface ProcessedEventRepo extends ProcessedEventRepoCM<ProcessedEvent> {
}
