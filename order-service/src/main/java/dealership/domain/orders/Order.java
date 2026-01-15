package dealership.domain.orders;

import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.exceptions.DomainValidationException;
import lombok.Getter;

import java.util.UUID;

public class Order {
    @Getter
    private final UUID id;

    @Getter
    private final boolean isWithConfiguration;

    @Getter
    private final UUID clientID;

    @Getter
    private final UUID managerID;

    @Getter
    private final CarConfiguration requestedConfiguration;

    @Getter
    private Car car;

    private OrderState state;

    public Order(
            UUID id,
            boolean isWithConfiguration,
            UUID clientID,
            UUID managerID,
            CarConfiguration requestedConfiguration,
            Car car
    ) {
        if (id == null) {
            throw new DomainValidationException("Order id must not be null.");
        }
        if (clientID == null) {
            throw new DomainValidationException("Client id must not be null.");
        }
        if (managerID == null) {
            throw new DomainValidationException("Manager id must not be null.");
        }
        if (requestedConfiguration == null) {
            throw new DomainValidationException("Requested configuration must not be null.");
        }
        if (!isWithConfiguration && car == null) {
            throw new DomainValidationException("In-stock order must reference a car.");
        }
        if (!isWithConfiguration && car.getConfiguration() != requestedConfiguration) {
            throw new DomainValidationException("Requested configuration must match assigned car configuration.");
        }

        this.id = id;
        this.isWithConfiguration = isWithConfiguration;
        this.clientID = clientID;
        this.managerID = managerID;
        this.requestedConfiguration = requestedConfiguration;
        this.car = car;
        this.state = new PlacesOrderState(this);
    }

    public void assignCar(Car car) {
        if (car == null) {
            throw new DomainValidationException("Car must not be null.");
        }
        this.car = car;
    }

    public void markApproved() {
        state.markApproved();
    }

    public void markWaitingForPayment() {
        state.markWaitingForPayment();
    }

    public void markPaid() {
        state.markPaid();
    }

    public void markReadyForPickup() {
        state.markReadyForPickup();
    }

    public void markWaitingForDelivery() {
        state.markWaitingForDelivery();
    }

    public void markCompleted() {
        state.markCompleted();
    }

    public void markCancelled() {
        state.markCancelled();
    }

    public void rejectByStorage() {
        if (!(state instanceof PaidOrderState)) {
            throw new IllegalStateException(
                    "Cannot reject by storage: order is not in paid state."
            );
        }
        transitionTo(new CancelledOrderState(this));
    }

    public String getStatus() {
        return state.getName();
    }

    void transitionTo(OrderState newState) {
        if (newState == null) {
            throw new DomainValidationException("New state must not be null.");
        }
        this.state = newState;
    }
}
