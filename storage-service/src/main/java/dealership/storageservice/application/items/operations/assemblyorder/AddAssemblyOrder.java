package dealership.storageservice.application.items.operations.assemblyorder;

import java.util.List;
import java.util.UUID;

public final class AddAssemblyOrder {

    private AddAssemblyOrder() {
    }

    public record Request(
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID carModelId,
            List<UUID> requiredSlotVariantIds,
            UUID warehouseManagerId
    ) {
    }

    public record Response(UUID assemblyOrderId) {
    }
}
