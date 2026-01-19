package dealership.storageservice.application.items.operations.car;

import dealership.storageservice.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckInStockCar {

    private CheckInStockCar() { }

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



