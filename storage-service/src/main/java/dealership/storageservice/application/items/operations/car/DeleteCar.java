package dealership.storageservice.application.items.operations.car;

import java.util.UUID;

public final class DeleteCar {

    private DeleteCar() { }

    public record Request(UUID carId) {}

    public record Response() {}
}



