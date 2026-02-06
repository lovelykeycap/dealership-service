package dealership.messaging.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderRejectedEvent(
        UUID orderId,
        String traceId,
        String reason,
        Instant occurredAt
) {
}
