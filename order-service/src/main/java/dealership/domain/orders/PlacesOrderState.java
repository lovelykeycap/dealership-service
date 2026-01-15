package dealership.domain.orders;

public class PlacesOrderState implements OrderState {

    private final Order order;

    public PlacesOrderState(Order order) {
        this.order = order;
    }

    @Override
    public void markApproved() {
        if (order.isWithConfiguration()) {
            order.transitionTo(new ApprovedByWarehouseOrderState(order));
            return;
        }
        order.transitionTo(new ApprovedByManagerOrderState(order));
    }

    @Override
    public void markWaitingForPayment() {
        throw new IllegalStateException("Cannot mark as waiting for payment: order has not been approved yet.");
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
        return "Placed";
    }
}
