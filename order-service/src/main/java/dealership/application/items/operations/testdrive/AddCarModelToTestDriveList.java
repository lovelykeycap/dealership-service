package dealership.application.items.operations.testdrive;

import java.util.UUID;

public final class AddCarModelToTestDriveList {

    private AddCarModelToTestDriveList() { }

    public record Request(UUID modelId) {}

    public record Response(UUID modelId) {}
}

