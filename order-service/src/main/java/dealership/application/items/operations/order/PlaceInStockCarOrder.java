package dealership.application.items.operations.order;

import dealership.application.items.dtos.CurrentUser;

import java.util.UUID;

public final class PlaceInStockCarOrder {

    private PlaceInStockCarOrder() { }

    public record Request(
            CurrentUser currentUser,
            UUID carId
    ) {}

    public record Response(UUID orderId) {}
}

