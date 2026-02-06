package dealership.messaging.contracts;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record OrderSentForApprovalEvent(
        UUID orderId,
        String traceId,
        SourceOrderType sourceOrderType,
        UUID carId,
        UUID carModelId,
        Set<UUID> slotVariantIds,
        Instant occurredAt
) {
}
