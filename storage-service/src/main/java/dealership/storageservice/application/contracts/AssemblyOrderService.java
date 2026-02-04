package dealership.storageservice.application.contracts;

import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.storageservice.application.items.operations.assemblyorder.AddAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrderList;
import dealership.storageservice.application.items.operations.assemblyorder.DeleteAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.UpdateAssemblyOrder;

import java.util.UUID;

public interface AssemblyOrderService {

    AddAssemblyOrder.Response addAssemblyOrder(AddAssemblyOrder.Request request);

    CheckAssemblyOrder.Response checkAssemblyOrder(CheckAssemblyOrder.Request request);

    CheckAssemblyOrderList.Response checkAssemblyOrderList(CheckAssemblyOrderList.Request request);

    UpdateAssemblyOrder.Response updateAssemblyOrder(UpdateAssemblyOrder.Request request);

    DeleteAssemblyOrder.Response deleteAssemblyOrder(DeleteAssemblyOrder.Request request);

    void createOrUpdateFromApprovalEvent(OrderSentForApprovalEvent event);

    void markAssembledBySourceOrderId(UUID sourceOrderId);

    void markFailedBySourceOrderId(UUID sourceOrderId);
}
