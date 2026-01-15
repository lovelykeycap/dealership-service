package dealership.domain.orders;

public class WaitingForDeliveryOrderState implements OrderState {

    private final Order order;

    public WaitingForDeliveryOrderState(Order order) {
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
        order.transitionTo(new ReadyForPickupOrderState(order));
    }

    @Override
    public void markWaitingForDelivery() {
        throw new IllegalStateException("Cannot mark as waiting for delivery: order is already waiting for delivery.");
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
        return "Waiting for delivery";
    }
}
