package dealership.application.items.helpers;

import dealership.application.items.operations.order.UpdateOrder;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.orders.Order;

public class OrderChangeApplier {
    public void applyChange(Order order, UpdateOrder.Change change) {
        if (change instanceof UpdateOrder.AssignCar) {
            throw new DomainValidationException(
                    "AssignCar must be handled in OrderService before applying order changes."
            );
        }
        if (change instanceof UpdateOrder.MarkApproved) {
            order.markApproved();
            return;
        }
        if (change instanceof UpdateOrder.MarkWaitingForPayment) {
            order.markWaitingForPayment();
            return;
        }
        if (change instanceof UpdateOrder.MarkPaid) {
            order.markPaid();
            return;
        }
        if (change instanceof UpdateOrder.MarkWaitingForDelivery) {
            order.markWaitingForDelivery();
            return;
        }
        if (change instanceof UpdateOrder.MarkReadyForPickup) {
            order.markReadyForPickup();
            return;
        }
        if (change instanceof UpdateOrder.MarkCompleted) {
            order.markCompleted();
            return;
        }
        if (change instanceof UpdateOrder.MarkCancelled) {
            order.markCancelled();
            return;
        }

        throw new DomainValidationException(
                "Unsupported order change type: " + change.getClass().getSimpleName()
        );
    }
}
