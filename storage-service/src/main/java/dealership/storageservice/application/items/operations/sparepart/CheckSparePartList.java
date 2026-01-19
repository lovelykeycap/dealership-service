package dealership.storageservice.application.items.operations.sparepart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CheckSparePartList {

    private CheckSparePartList() { }

    public record Request() {}

    public record Response(List<SparePartDto> spareParts) {}

    public record SparePartDto(
            UUID sparePartId,
            String slotType,
            String value,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {}
}


