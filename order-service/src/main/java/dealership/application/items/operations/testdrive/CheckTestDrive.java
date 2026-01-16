package dealership.application.items.operations.testdrive;

import java.time.LocalDateTime;
import java.util.UUID;

public final class CheckTestDrive {

    private CheckTestDrive() { }

    public record Request(UUID id) {}

    public record Response(
            UUID id,
            UUID clientId,
            UUID modelId,
            LocalDateTime startAt
    ) {}
}

