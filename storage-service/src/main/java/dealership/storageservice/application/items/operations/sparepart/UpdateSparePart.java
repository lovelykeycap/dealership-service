package dealership.storageservice.application.items.operations.sparepart;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UpdateSparePart {
    private UpdateSparePart() {}

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChangeSlotType.class, name = "changeSlotType"),
            @JsonSubTypes.Type(value = ChangeValue.class, name = "changeValue"),
            @JsonSubTypes.Type(value = ChangePrice.class, name = "changePrice"),
            @JsonSubTypes.Type(value = ChangeCompatibleCarModelIds.class, name = "changeCompatibleCarModelIds")
    })
    public sealed interface Change permits
            ChangeSlotType,
            ChangeValue,
            ChangePrice,
            ChangeCompatibleCarModelIds
    {}

    public record ChangeSlotType(String slotType) implements Change {}
    public record ChangeValue(String value) implements Change {}
    public record ChangePrice(BigDecimal price) implements Change {}
    public record ChangeCompatibleCarModelIds(Set<UUID> compatibleCarModelIds) implements Change {}

    public record Request(UUID sparePartId, List<Change> changes) {}

    public record Response(UUID sparePartId) {}
}


