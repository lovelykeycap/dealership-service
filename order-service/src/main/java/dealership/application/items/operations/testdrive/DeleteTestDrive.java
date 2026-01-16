package dealership.application.items.operations.testdrive;

import java.util.UUID;

public final class DeleteTestDrive {

    private DeleteTestDrive() { }

    public record Request(UUID id) {}

    public record Response(UUID id) {}
}

