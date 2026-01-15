package dealership.domain.orders;

public class CancelledOrderState implements OrderState {

    private final Order order;

    public CancelledOrderState(Order order) {
        this.order = order;
    }

    @Override
    public void markApproved() {
        throw new IllegalStateException("Cannot approve by manager: order is cancelled.");
    }

    @Override
    public void markWaitingForPayment() {
        throw new IllegalStateException("Cannot request payment: order is cancelled.");
    }

    @Override
    public void markPaid() {
        throw new IllegalStateException("Cannot mark as paid: order is cancelled.");
    }

    @Override
    public void markReadyForPickup() {
        throw new IllegalStateException("Cannot mark as ready for pickup: order is cancelled.");
    }

    @Override
    public void markWaitingForDelivery() {
        throw new IllegalStateException("Cannot mark as waiting for delivery: order is cancelled.");
    }

    @Override
    public void markCompleted() {
        throw new IllegalStateException("Cannot complete order: order is cancelled.");
    }

    @Override
    public void markCancelled() {
        throw new IllegalStateException("Cannot cancel order: order is already cancelled.");
    }

    @Override
    public String getName() {
        return "Cancelled";
    }
}
