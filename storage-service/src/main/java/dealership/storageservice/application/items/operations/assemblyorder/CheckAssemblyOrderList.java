package dealership.storageservice.application.items.operations.assemblyorder;

import java.util.List;
import java.util.UUID;

public final class CheckAssemblyOrderList {

    private CheckAssemblyOrderList() {
    }

    public record Request() {
    }

    public record Response(List<AssemblyOrderDto> assemblyOrders) {
    }

    public record AssemblyOrderDto(
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
