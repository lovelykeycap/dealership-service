package dealership.storageservice.application.items.operations.assemblyorder;

import java.util.List;
import java.util.UUID;

public final class CheckAssemblyOrder {

    private CheckAssemblyOrder() {
    }

    public record Request(UUID assemblyOrderId) {
    }

    public record Response(
            UUID assemblyOrderId,
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID carModelId,
            List<UUID> requiredSlotVariantIds,
            UUID warehouseManagerId,
            String status
    ) {
    }
}
