package dealership.application.items.operations.testdrive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class UpdateTestDrive {

    private UpdateTestDrive() { }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChangeCarModel.class, name = "changeCarModel"),
            @JsonSubTypes.Type(value = ChangeStartAt.class, name = "changeStartAt")
    })
    public sealed interface Change permits
            ChangeCarModel,
            ChangeStartAt
    {}

    public record ChangeCarModel(UUID modelId) implements Change {}

    public record ChangeStartAt(LocalDateTime startAt) implements Change {}

    public record Request(UUID id, List<Change> changes) {}

    public record Response(UUID id) {}
}

