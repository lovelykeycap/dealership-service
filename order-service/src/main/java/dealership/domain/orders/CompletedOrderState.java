package dealership.domain.orders;

public class CompletedOrderState implements OrderState {

    @Override
    public void markApproved() {
        throw new IllegalStateException("Cannot mark as approved: order is completed.");
    }

    @Override
    public void markWaitingForPayment() {
        throw new IllegalStateException("Cannot mark as waiting for payment: order is completed.");
    }

    @Override
    public void markPaid() {
        throw new IllegalStateException("Cannot mark as paid: order is completed.");
    }

    @Override
    public void markReadyForPickup() {
        throw new IllegalStateException("Cannot mark as ready for pickup: order is completed.");
    }

    @Override
    public void markWaitingForDelivery() {
        throw new IllegalStateException("Cannot mark as waiting for delivery: order is completed.");
    }

    @Override
    public void markCompleted() {
        throw new IllegalStateException("Cannot mark as completed: order is already completed.");
    }

    @Override
    public void markCancelled() {
        throw new IllegalStateException("Cannot mark as cancelled: order is completed.");
    }

    @Override
    public String getName() {
        return "Completed";
    }
}
