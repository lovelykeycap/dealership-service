package dealership.application.items.operations.order;

import dealership.application.items.dtos.CurrentUser;
import dealership.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckOrder {

    private CheckOrder() { }

    public record Request(UUID orderId, CurrentUser currentUser) {}

    public record Response(
            UUID orderId,
            boolean withConfiguration,
            UUID clientId,
            UUID managerId,
            UUID modelId,
            UUID carId,
            BigDecimal price,
            String status,
            List<SlotDto> selectedSlots
    ) {}
}

