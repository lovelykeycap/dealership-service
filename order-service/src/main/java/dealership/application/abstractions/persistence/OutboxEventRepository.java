package dealership.application.abstractions.persistence;

import dealership.application.items.events.NewOutboxEvent;
import dealership.application.items.events.PendingOutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    void save(NewOutboxEvent event);

    List<PendingOutboxEvent> findReadyForPublish(Instant now, int limit);

    void markSent(UUID outboxEventId, Instant publishedAt);

    void markFailed(UUID outboxEventId, String lastError, Instant nextAttemptAt);
}
