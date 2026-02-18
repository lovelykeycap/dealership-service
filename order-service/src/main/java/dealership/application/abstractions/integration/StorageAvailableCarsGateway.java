package dealership.application.abstractions.integration;

import dealership.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StorageAvailableCarsGateway {

    List<AvailableCarData> getAvailableCars();

    AvailableCarData getAvailableCarById(UUID carId);

    record AvailableCarData(
            UUID carId,
            UUID modelId,
            String brand,
            String model,
            BigDecimal price,
            String status,
            List<SlotDto> selectedSlots
    ) {}
}
