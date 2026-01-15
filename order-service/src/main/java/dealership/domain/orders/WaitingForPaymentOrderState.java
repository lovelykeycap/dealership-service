package dealership.domain.orders;

public class WaitingForPaymentOrderState implements OrderState {

    private final Order order;

    public WaitingForPaymentOrderState(Order order) {
        this.order = order;
    }

    @Override
    public void markApproved() {
        throw new IllegalStateException("Cannot mark as approved: order is already approved.");
    }

    @Override
    public void markWaitingForPayment() {
        throw new IllegalStateException("Cannot mark as waiting for payment: order is already waiting for payment.");
    }

    @Override
    public void markPaid() {
        order.transitionTo(new PaidOrderState(order));
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
        throw new IllegalStateException("Cannot mark as completed: order is not ready for pickup.");
    }

    @Override
    public void markCancelled() {
        order.transitionTo(new CancelledOrderState(order));
    }

    @Override
    public String getName() {
        return "Waiting for payment";
    }
}
