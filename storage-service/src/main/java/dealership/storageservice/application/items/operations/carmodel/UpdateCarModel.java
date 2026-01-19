package dealership.storageservice.application.items.operations.carmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dealership.storageservice.application.items.dtos.SlotRefDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UpdateCarModel {
    private UpdateCarModel() {}

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChangeBrand.class, name = "changeBrand"),
            @JsonSubTypes.Type(value = Rename.class, name = "rename"),
            @JsonSubTypes.Type(value = ChangeBasePrice.class, name = "changeBasePrice"),
            @JsonSubTypes.Type(value = ChangeBaseSlots.class, name = "changeBaseSlots"),
            @JsonSubTypes.Type(value = ChangeReplaceableSlots.class, name = "changeReplaceableSlots")
    })
    public sealed interface Change permits
            ChangeBrand,
            Rename,
            ChangeBasePrice,
            ChangeBaseSlots,
            ChangeReplaceableSlots
    {}

    public record ChangeBrand(String brand) implements Change {}
    public record Rename(String name) implements Change {}
    public record ChangeBasePrice(BigDecimal price) implements Change {}
    public record ChangeBaseSlots(List<SlotRefDto> baseSlots) implements Change {}
    public record ChangeReplaceableSlots(Set<String> replaceableSlots) implements Change {}

    public record Request(UUID modelId, List<Change> changes) {}

    public record Response(UUID modelId) {}
}



