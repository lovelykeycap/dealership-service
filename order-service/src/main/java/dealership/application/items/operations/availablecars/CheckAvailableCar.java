package dealership.application.items.operations.availablecars;

import dealership.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckAvailableCar {

    private CheckAvailableCar() { }

    public record Request(UUID carId) {}

    public record Response(
            UUID carId,
            UUID modelId,
            String brand,
            String model,
            BigDecimal price,
            String status,
            List<SlotDto> selectedSlots
    ) {}
}
