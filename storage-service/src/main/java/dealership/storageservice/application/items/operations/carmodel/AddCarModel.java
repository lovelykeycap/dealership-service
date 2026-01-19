package dealership.storageservice.application.items.operations.carmodel;

import dealership.storageservice.application.items.dtos.SlotRefDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AddCarModel {

    private AddCarModel() { }

    public record Request(
            String brand,
            String name,
            BigDecimal price,
            List<SlotRefDto> baseSlots,
            Set<String> replaceableSlots
    ) {}

    public record Response(UUID modelId) {}
}



