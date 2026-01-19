package dealership.storageservice.application.items.operations.sparepartstock;

import java.util.UUID;

public final class RemoveSparePartStock {

    private RemoveSparePartStock() { }

    public record Request(UUID sparePartId, int quantity) {}

    public record Response(UUID sparePartId) {}
}


