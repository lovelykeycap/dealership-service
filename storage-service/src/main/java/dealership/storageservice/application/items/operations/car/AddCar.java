package dealership.storageservice.application.items.operations.car;

import dealership.storageservice.application.items.dtos.SlotRefDto;

import java.util.List;
import java.util.UUID;

public final class AddCar {

    private AddCar() { }

    public record Request(
            UUID modelId,
            List<SlotRefDto> selectedSlots
    ) {}

    public record Response(UUID carId) {}
}



