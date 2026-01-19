package dealership.storageservice.application.items.operations.carmodel;

import dealership.storageservice.application.items.dtos.SlotDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CheckCarModel {

    private CheckCarModel() { }

    public record Request(UUID modelId) {}

    public record Response(
            UUID modelId,
            String brand,
            String name,
            BigDecimal price,
            List<SlotDto> baseSlots,
            Set<String> replaceableSlots
    ) {}
}



