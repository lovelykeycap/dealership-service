package dealership.application.items.operations.testdrive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class CheckTestDriveList {

    private CheckTestDriveList() { }

    public record Request() {}

    public record Response(List<TestDriveRequestDto> requests) {}

    public record TestDriveRequestDto(
            UUID id,
            UUID clientId,
            UUID modelId,
            LocalDateTime startAt
    ) {}
}

