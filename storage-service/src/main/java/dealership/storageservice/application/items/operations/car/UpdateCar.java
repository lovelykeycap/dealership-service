package dealership.storageservice.application.items.operations.car;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dealership.storageservice.application.items.dtos.SlotRefDto;

import java.util.List;
import java.util.UUID;

public final class UpdateCar {

    private UpdateCar() { }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChangeSelectedSlots.class, name = "changeSelectedSlots"),
            @JsonSubTypes.Type(value = ChangeStatus.class, name = "changeStatus")
    })

    public sealed interface Change permits
            ChangeSelectedSlots,
            ChangeStatus
    {}

    public record ChangeSelectedSlots(List<SlotRefDto> selectedSlots) implements Change {}

    public record ChangeStatus(String status) implements Change {}

    public record Request(UUID carId, List<Change> changes) {}

    public record Response(UUID carId) {}
}



