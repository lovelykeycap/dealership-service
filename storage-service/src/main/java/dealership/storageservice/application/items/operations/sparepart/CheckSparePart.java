package dealership.storageservice.application.items.operations.sparepart;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public final class CheckSparePart {

    private CheckSparePart() { }

    public record Request(UUID sparePartId) {}

    public record Response(
            UUID sparePartId,
            String slotType,
            String value,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {}
}


