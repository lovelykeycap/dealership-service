package dealership.domain.orders;

public interface OrderState {
    void markApproved();
    void markWaitingForPayment();
    void markPaid();
    void markReadyForPickup();
    void markWaitingForDelivery();
    void markCompleted();
    void markCancelled();

    String getName();
}
