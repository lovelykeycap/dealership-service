package dealership.application.items.events;

import java.util.UUID;

public record NewOutboxEvent(
        UUID id,
        UUID aggregateId,
        String aggregateType,
        String eventType,
        String exchangeName,
        String routingKey,
        String payload,
        String traceId
) {
}
