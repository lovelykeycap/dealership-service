package dealership.storageservice.application.items.operations.sparepartstock;

import java.util.List;
import java.util.UUID;

public final class CheckSparePartStockList {

    private CheckSparePartStockList() { }

    public record Request() {}

    public record Response(List<SparePartStockDto> sparePartStocks) {}

    public record SparePartStockDto(UUID sparePartId, int quantity) {}
}


