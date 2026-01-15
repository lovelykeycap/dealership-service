package dealership.domain.orders;

public class ReadyForPickupOrderState implements OrderState {

    private final Order order;

    public ReadyForPickupOrderState(Order order) {
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
        throw new IllegalStateException("Cannot mark as ready for pickup: order is already ready for pickup.");
    }

    @Override
    public void markWaitingForDelivery() {
        throw new IllegalStateException("Cannot mark as waiting for delivery: order is already ready for pickup.");
    }

    @Override
    public void markCompleted() {
        order.transitionTo(new CompletedOrderState());
    }

    @Override
    public void markCancelled() {
        throw new IllegalStateException("Cannot mark as cancelled: order is ready for pickup.");
    }

    @Override
    public String getName() {
        return "Ready for pickup";
    }
}
