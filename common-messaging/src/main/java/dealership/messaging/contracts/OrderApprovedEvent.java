package dealership.messaging.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderApprovedEvent(
        UUID orderId,
        String traceId,
        UUID assemblyOrderId,
        Instant occurredAt
) {
}
