package project.eventbookingservice.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;
import project.eventbookingservice.client.BookingClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxBookingTask {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final BookingClient bookingClient;

    private final ObjectMapper objectMapper;

    public void processOutboxRelay(List<OutBoxDto> batch, int workerId) {
        try {
            log.info("Worker {} processing {} records", workerId, batch.size());

            List<CompletableFuture<String>> futures = new ArrayList<>();

            Map<String, String> outBoxesMapMessage = new HashMap<>();

            for (OutBoxDto outboxDto : batch) {
                outBoxesMapMessage.putIfAbsent(outboxDto.getId(), objectMapper.writeValueAsString(outboxDto));
            }

            for (OutBoxDto outboxDto : batch) {
                CompletableFuture<String> future = kafkaTemplate.send(outboxDto.getTopic(), outBoxesMapMessage.get(outboxDto.getId()))
                        .handle((result, ex) -> {
                            if (ex == null) {
                                return outboxDto.getId();
                            } else {
                                log.error("Worker {} failed to send outbox ID {} to Kafka: {}",
                                        workerId, outboxDto.getId(), ex.getMessage());
                                return null;
                            }
                        });
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<String> successfulIds = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

            if (!successfulIds.isEmpty()) {
                log.info("Worker {} sending API request to update {} outboxes to is processed to true", workerId, successfulIds.size());

                UpdateOutboxDto updateOutboxStatusDto = UpdateOutboxDto.builder()
                        .ids(successfulIds)
                        .isSent(true)
                        .build();

                bookingClient.updateSentOutbox(updateOutboxStatusDto);
                log.info("Worker {} batch update success!", workerId);
            }

        } catch (Exception e) {
            log.error("Worker {} failed during batch processing: {}", workerId, e.getMessage());
        }
    }
}
