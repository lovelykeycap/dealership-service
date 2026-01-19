package dealership.storageservice.application.items.operations.carmodel;

import java.util.UUID;

public final class DeleteCarModel {

    private DeleteCarModel() { }

    public record Request(UUID modelId) {}

    public record Response(UUID modelId) {}
}



