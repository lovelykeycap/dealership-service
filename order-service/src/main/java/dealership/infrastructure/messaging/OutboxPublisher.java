package dealership.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dealership.application.abstractions.persistence.OutboxEventRepository;
import dealership.application.items.events.PendingOutboxEvent;
import dealership.messaging.contracts.OrderSentForApprovalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.outbox.publisher.batch-size:50}")
    private int batchSize;

    @Value("${messaging.outbox.publisher.retry-delay-seconds:5}")
    private long retryDelaySeconds;

    @Scheduled(
            initialDelayString = "${messaging.outbox.publisher.initial-delay-ms:2000}",
            fixedDelayString = "${messaging.outbox.publisher.fixed-delay-ms:2000}"
    )
    public void publishReadyEvents() {
        Instant now = Instant.now();
        int safeBatchSize = Math.max(1, batchSize);
        long safeRetryDelaySeconds = Math.max(1L, retryDelaySeconds);

        List<PendingOutboxEvent> events = outboxEventRepository.findReadyForPublish(now, safeBatchSize);
        for (PendingOutboxEvent event : events) {
            publishSingleEvent(event, safeRetryDelaySeconds);
        }
    }

    private void publishSingleEvent(PendingOutboxEvent event, long retryDelaySeconds) {
        try {
            Object payload = deserializePayload(event);
            rabbitTemplate.convertAndSend(event.exchangeName(), event.routingKey(), payload);
            outboxEventRepository.markSent(event.id(), Instant.now());

            log.info(
                    "Outbox event published. id={}, eventType={}, traceId={}",
                    event.id(),
                    event.eventType(),
                    event.traceId()
            );
        } catch (Exception ex) {
            Instant nextAttemptAt = Instant.now().plusSeconds(retryDelaySeconds);
            outboxEventRepository.markFailed(event.id(), ex.getMessage(), nextAttemptAt);

            log.warn(
                    "Outbox publish failed. id={}, eventType={}, attempts={}, nextAttemptAt={}, error={}",
                    event.id(),
                    event.eventType(),
                    event.attempts() + 1,
                    nextAttemptAt,
                    ex.getMessage()
            );
        }
    }

    private Object deserializePayload(PendingOutboxEvent event) throws Exception {
        if (OrderSentForApprovalEvent.class.getSimpleName().equals(event.eventType())) {
            return objectMapper.readValue(event.payload(), OrderSentForApprovalEvent.class);
        }
        throw new IllegalStateException(
                "Unsupported outbox event type: \"%s\"".formatted(event.eventType())
        );
    }
}
