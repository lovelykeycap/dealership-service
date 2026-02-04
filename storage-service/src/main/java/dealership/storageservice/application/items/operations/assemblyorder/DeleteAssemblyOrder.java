package dealership.storageservice.application.items.operations.assemblyorder;

import java.util.UUID;

public final class DeleteAssemblyOrder {

    private DeleteAssemblyOrder() {
    }

    public record Request(UUID assemblyOrderId) {
    }

    public record Response(UUID assemblyOrderId) {
    }
}
