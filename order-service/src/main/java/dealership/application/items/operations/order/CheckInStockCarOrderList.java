package dealership.application.items.operations.order;

import dealership.application.items.dtos.CurrentUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckInStockCarOrderList {

    private CheckInStockCarOrderList() { }

    public record Request(CurrentUser currentUser) {}

    public record Response(List<InStockCarOrderDto> orders) {}

    public record InStockCarOrderDto(
            UUID orderId,
            UUID clientId,
            UUID managerId,
            UUID carId,
            BigDecimal price,
            String status
    ) {}
}

