package dealership.domain.orders;

public class ApprovedByWarehouseOrderState implements OrderState {

    private final Order order;

    public ApprovedByWarehouseOrderState(Order order) {
        this.order = order;
    }

    @Override
    public void markApproved() {
        throw new IllegalStateException("Cannot mark as approved: order is already approved by warehouse.");
    }

    @Override
    public void markWaitingForPayment() {
        order.transitionTo(new WaitingForPaymentOrderState(order));
    }

    @Override
    public void markPaid() {
        throw new IllegalStateException("Cannot mark as paid: payment has not been requested yet.");
    }

    @Override
    public void markReadyForPickup() {
        throw new IllegalStateException("Cannot mark as ready for pickup: order has not been paid.");
    }

    @Override
    public void markWaitingForDelivery() {
        throw new IllegalStateException("Cannot mark as waiting for delivery: order has not been paid.");
    }

    @Override
    public void markCompleted() {
        throw new IllegalStateException("Cannot complete order: order is not ready for pickup.");
    }

    @Override
    public void markCancelled() {
        order.transitionTo(new CancelledOrderState(order));
    }

    @Override
    public String getName() {
        return "Approved by warehouse";
    }
}
