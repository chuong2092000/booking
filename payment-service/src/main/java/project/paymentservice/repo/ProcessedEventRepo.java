package project.paymentservice.repo;

import org.springframework.stereotype.Repository;

import project.commonutils.repo.ProcessedEventRepoCM;
import project.paymentservice.entity.ProcessedEvent;

@Repository
public interface ProcessedEventRepo extends ProcessedEventRepoCM<ProcessedEvent> {
}
