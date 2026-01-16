package dealership.application.items.operations.testdrive;

import java.time.LocalDateTime;
import java.util.UUID;

public final class CreateTestDrive {

    private CreateTestDrive() { }

    public record Request(
            UUID clientId,
            UUID modelId,
            LocalDateTime startAt
    ) {}

    public record Response(UUID id) {}
}

