package dealership.application.items.operations.order;

import java.util.UUID;

public final class DeleteOrder {

    private DeleteOrder() { }

    public record Request(UUID orderId) {}

    public record Response(UUID orderId) {}
}

