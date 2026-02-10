package dealership.application.items.events;

import java.util.UUID;

public record PendingOutboxEvent(
        UUID id,
        String eventType,
        String exchangeName,
        String routingKey,
        String payload,
        String traceId,
        int attempts
) {
}
