package dealership.domain.entities.cars;

import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.DomainValidationException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Car {
    private final UUID id;

    private final CarConfiguration configuration;

    private CarStockStatus status;

    public Car(UUID id, CarConfiguration configuration) {
        if (id == null) {
            throw new DomainValidationException("Car id must not be null.");
        }
        if (configuration == null) {
            throw new DomainValidationException("Car configuration must not be null.");
        }

        this.id = id;
        this.configuration = configuration;
        this.status = CarStockStatus.AVAILABLE;
    }

    public Money getPrice() {
        return configuration.calculatePrice();
    }

    public void changeStatus(CarStockStatus status) {
        if (status == null) {
            throw new DomainValidationException("Car status must not be null.");
        }

        this.status = status;
    }
}
