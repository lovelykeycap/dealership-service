package dealership.storageservice.application.items.operations.sparepart;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public final class AddSparePart {

    private AddSparePart() { }

    public record Request(
            String slotType,
            String value,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {}

    public record Response(UUID sparePartId) {}
}


