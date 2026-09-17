package project.eventbookingservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.commondto.dto.OutBoxDto;
import project.eventbookingservice.client.BookingClient;
import project.eventbookingservice.task.OutboxBookingTask;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class BookingProducer {

    private final BookingClient bookingClient;

    private final OutboxBookingTask task;

    @Value("${job.worker.count:3}")
    private int numberOfWorkers;

    @Scheduled(fixedDelayString = "${job.scanner.delay:1000}")
    public void executeTask() {
        log.info("Start exec booking job...");

        List<OutBoxDto> outboxes = bookingClient.getOutBoxes(50);

        if (outboxes.isEmpty()) {
            log.info("End exec...");
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        int pageSize = (int) Math.ceil((double) outboxes.size() / numberOfWorkers);

        for (int i = 0; i < numberOfWorkers; i++) {
            int start = i * pageSize;
            int end = Math.min(start + pageSize, outboxes.size());

            if (start < end) {
                List<OutBoxDto> subList = outboxes.subList(start, end);
                int workerId = i + 1;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                        task.processOutboxRelay(subList, workerId)
                );
                futures.add(future);
            }
        }

        log.info("Execution: processing {} records with {} workers", outboxes.size(), numberOfWorkers);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(2, TimeUnit.MINUTES)
                .join();
        log.info("Execution: processed success {} records.", outboxes.size());
    }
}
