package project.eventpaymentservice.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.commondto.dto.OutBoxDto;
import project.commondto.dto.UpdateOutboxDto;
import project.eventpaymentservice.client.PaymentClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPaymentTask {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PaymentClient paymentClient;

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

                paymentClient.updateStatusOutboxes(updateOutboxStatusDto);
                log.info("Worker {} batch update success!", workerId);
            }

        } catch (Exception e) {
            log.error("Worker {} failed during batch processing: {}", workerId, e.getMessage());
        }
    }
}
