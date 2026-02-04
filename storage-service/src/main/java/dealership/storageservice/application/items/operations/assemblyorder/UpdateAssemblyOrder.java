package dealership.storageservice.application.items.operations.assemblyorder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

public final class UpdateAssemblyOrder {

    private UpdateAssemblyOrder() {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChangeStatus.class, name = "changeStatus"),
            @JsonSubTypes.Type(value = AssignWarehouseManager.class, name = "assignWarehouseManager")
    })
    public sealed interface Change permits ChangeStatus, AssignWarehouseManager {
    }

    public record ChangeStatus(String status) implements Change {
    }

    public record AssignWarehouseManager(UUID warehouseManagerId) implements Change {
    }

    public record Request(UUID assemblyOrderId, Change change) {
    }

    public record Response(UUID assemblyOrderId) {
    }
}
