package dealership.application.items.operations.order;

import dealership.application.items.dtos.CurrentUser;
import dealership.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckConfiguredCarOrderList {

    private CheckConfiguredCarOrderList() { }

    public record Request(CurrentUser currentUser) {}

    public record Response(List<ConfiguredCarOrderDto> orders) {}

    public record ConfiguredCarOrderDto(
            UUID orderId,
            UUID clientId,
            UUID managerId,
            UUID modelId,
            BigDecimal price,
            String status,
            List<SlotDto> selectedSlots
    ) {}
}

