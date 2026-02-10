package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.OutboxEventRepository;
import dealership.application.items.events.NewOutboxEvent;
import dealership.application.items.events.PendingOutboxEvent;
import dealership.infrastructure.persistence.entity.jpa.OutboxEventJpa;
import dealership.infrastructure.persistence.repositories.jpa.OutboxEventSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OutboxEventRepositoryJpaAdapter implements OutboxEventRepository {

    private final OutboxEventSpringDataRepository outboxEventSpringDataRepository;

    @Override
    @Transactional
    public void save(NewOutboxEvent event) {
        outboxEventSpringDataRepository.save(
                new OutboxEventJpa(
                        event.id(),
                        event.aggregateId(),
                        event.aggregateType(),
                        event.eventType(),
                        event.exchangeName(),
                        event.routingKey(),
                        event.payload(),
                        event.traceId()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingOutboxEvent> findReadyForPublish(Instant now, int limit) {
        int safeLimit = Math.max(1, limit);
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "createdAt"));
        return outboxEventSpringDataRepository.findReadyForPublish(now, pageable)
                .stream()
                .map(this::toPendingOutboxEvent)
                .toList();
    }

    @Override
    @Transactional
    public void markSent(UUID outboxEventId, Instant publishedAt) {
        OutboxEventJpa event = findExistingOutboxEvent(outboxEventId);
        event.setStatus("SENT");
        event.setPublishedAt(publishedAt);
        event.setLastError(null);
        event.setNextAttemptAt(null);
        outboxEventSpringDataRepository.save(event);
    }

    @Override
    @Transactional
    public void markFailed(UUID outboxEventId, String lastError, Instant nextAttemptAt) {
        OutboxEventJpa event = findExistingOutboxEvent(outboxEventId);
        event.setStatus("FAILED");
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(trimError(lastError));
        event.setNextAttemptAt(nextAttemptAt);
        outboxEventSpringDataRepository.save(event);
    }

    private PendingOutboxEvent toPendingOutboxEvent(OutboxEventJpa entity) {
        return new PendingOutboxEvent(
                entity.getId(),
                entity.getEventType(),
                entity.getExchangeName(),
                entity.getRoutingKey(),
                entity.getPayload(),
                entity.getTraceId(),
                entity.getAttempts()
        );
    }

    private OutboxEventJpa findExistingOutboxEvent(UUID outboxEventId) {
        return outboxEventSpringDataRepository.findById(outboxEventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event \"%s\" not found.".formatted(outboxEventId)
                ));
    }

    private String trimError(String lastError) {
        if (lastError == null) {
            return null;
        }
        final int maxLength = 2000;
        if (lastError.length() <= maxLength) {
            return lastError;
        }
        return lastError.substring(0, maxLength);
    }
}
