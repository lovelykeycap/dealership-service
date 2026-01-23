package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStock;
import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStockList;
import dealership.storageservice.domain.entities.slots.SlotVariant;

public class SparePartStockDtoMapper {
    public CheckSparePartStock.Response toCheckSparePartStockResponse(SlotVariant slotVariant, int quantity) {
        return new CheckSparePartStock.Response(slotVariant.getId(), quantity);
    }

    public CheckSparePartStockList.SparePartStockDto toSparePartStockDto(SlotVariant slotVariant, int quantity) {
        return new CheckSparePartStockList.SparePartStockDto(slotVariant.getId(), quantity);
    }
}

