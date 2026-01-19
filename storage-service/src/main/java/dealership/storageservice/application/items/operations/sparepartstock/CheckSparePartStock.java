package dealership.storageservice.application.items.operations.sparepartstock;

import java.util.UUID;

public final class CheckSparePartStock {

    private CheckSparePartStock() { }

    public record Request(UUID sparePartId) {}

    public record Response(UUID sparePartId, int quantity) {}
}


