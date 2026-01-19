package dealership.application.items.operations.order;

import dealership.application.items.dtos.CurrentUser;
import dealership.application.items.dtos.SlotRefDto;

import java.util.List;
import java.util.UUID;

public final class PlaceConfiguredCarOrder {

    private PlaceConfiguredCarOrder() { }

    public record Request(
            CurrentUser currentUser,
            UUID modelId,
            List<SlotRefDto> selectedSlots
    ) {}

    public record Response(UUID orderId) {}
}

