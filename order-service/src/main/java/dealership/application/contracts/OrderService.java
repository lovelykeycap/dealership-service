package dealership.application.contracts;

import dealership.application.items.operations.order.CheckOrder;
import dealership.application.items.operations.order.CheckConfiguredCarOrderList;
import dealership.application.items.operations.order.CheckInStockCarOrderList;
import dealership.application.items.operations.order.DeleteOrder;
import dealership.application.items.operations.order.PlaceConfiguredCarOrder;
import dealership.application.items.operations.order.PlaceInStockCarOrder;
import dealership.application.items.operations.order.UpdateOrder;

public interface OrderService {

    PlaceInStockCarOrder.Response placeInStockCarOrder(PlaceInStockCarOrder.Request request);

    PlaceConfiguredCarOrder.Response placeConfiguredCarOrder(PlaceConfiguredCarOrder.Request request);

    CheckOrder.Response checkOrder(CheckOrder.Request request);

    CheckInStockCarOrderList.Response checkInStockCarOrderList(CheckInStockCarOrderList.Request request);

    CheckConfiguredCarOrderList.Response checkConfiguredCarOrderList(CheckConfiguredCarOrderList.Request request);

    UpdateOrder.Response updateOrder(UpdateOrder.Request request);

    DeleteOrder.Response deleteOrder(DeleteOrder.Request request);
}

