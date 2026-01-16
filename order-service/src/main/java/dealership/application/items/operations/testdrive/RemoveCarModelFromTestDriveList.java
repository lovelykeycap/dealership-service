package dealership.application.items.operations.testdrive;

import java.util.UUID;

public final class RemoveCarModelFromTestDriveList {

    private RemoveCarModelFromTestDriveList() { }

    public record Request(UUID modelId) {}

    public record Response(UUID modelId) {}
}

