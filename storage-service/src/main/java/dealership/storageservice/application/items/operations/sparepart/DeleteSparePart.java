package dealership.storageservice.application.items.operations.sparepart;

import java.util.UUID;

public final class DeleteSparePart {

    private DeleteSparePart() { }

    public record Request(UUID sparePartId) {}

    public record Response(UUID sparePartId) {}
}



