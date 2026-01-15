package dealership.domain.orders;

public class PaidOrderState implements OrderState {

    private final Order order;

    public PaidOrderState(Order order) {
        this.order = order;
    }

    @Override
    public void markApproved() {
        throw new IllegalStateException("Cannot mark as approved: order is already paid.");
    }

    @Override
    public void markWaitingForPayment() {
        throw new IllegalStateException("Cannot mark as waiting for payment: order is already paid.");
    }

    @Override
    public void markPaid() {
        throw new IllegalStateException("Cannot mark as paid: order is already paid.");
    }

    @Override
    public void markReadyForPickup() {
        if (order.isWithConfiguration()) {
            throw new IllegalStateException("Cannot mark as ready for pickup: order must be marked as waiting for delivery first.");
        }
        order.transitionTo(new ReadyForPickupOrderState(order));
    }

    @Override
    public void markWaitingForDelivery() {
        if (!order.isWithConfiguration()) {
            throw new IllegalStateException("Cannot mark as waiting for delivery: in-stock order does not require delivery.");
        }
        order.transitionTo(new WaitingForDeliveryOrderState(order));
    }

    @Override
    public void markCompleted() {
        throw new IllegalStateException("Cannot mark as completed: order is not ready for pickup.");
    }

    @Override
    public void markCancelled() {
        throw new IllegalStateException("Cannot mark as cancelled: paid order cannot be cancelled.");
    }

    @Override
    public String getName() {
        return "Paid";
    }
}
