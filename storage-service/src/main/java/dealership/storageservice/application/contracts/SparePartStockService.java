package dealership.storageservice.application.contracts;

import dealership.storageservice.application.items.operations.sparepartstock.AddSparePartStock;
import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStock;
import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStockList;
import dealership.storageservice.application.items.operations.sparepartstock.RemoveSparePartStock;

public interface SparePartStockService {

    AddSparePartStock.Response addSparePartStock(AddSparePartStock.Request request);

    CheckSparePartStock.Response checkSparePartStock(CheckSparePartStock.Request request);

    CheckSparePartStockList.Response checkSparePartStockList(CheckSparePartStockList.Request request);

    RemoveSparePartStock.Response removeSparePartStock(RemoveSparePartStock.Request request);
}
