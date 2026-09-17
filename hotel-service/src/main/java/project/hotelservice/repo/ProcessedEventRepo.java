package project.hotelservice.repo;

import org.springframework.stereotype.Repository;

import project.commonutils.repo.ProcessedEventRepoCM;
import project.hotelservice.entity.ProcessedEvent;

@Repository
public interface ProcessedEventRepo extends ProcessedEventRepoCM<ProcessedEvent> {
}
