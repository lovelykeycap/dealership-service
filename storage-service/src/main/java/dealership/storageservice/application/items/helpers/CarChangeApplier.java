package dealership.storageservice.application.items.helpers;

import dealership.storageservice.application.items.operations.car.UpdateCar;
import dealership.storageservice.domain.entities.cars.Car;
import dealership.storageservice.domain.entities.cars.CarStockStatus;
import dealership.storageservice.domain.exceptions.DomainValidationException;

public class CarChangeApplier {
    public void applyChange(Car car, UpdateCar.Change change) {
        if (change instanceof UpdateCar.ChangeStatus changeStatus) {
            car.changeStatus(parseCarStockStatus(changeStatus.status()));
            return;
        }

        if (change instanceof UpdateCar.ChangeSelectedSlots) {
            throw new DomainValidationException(
                    "ChangeSelectedSlots must be handled in InStockCarService before applying car changes."
            );
        }

        throw new DomainValidationException(
                "Unsupported car change type: " + change.getClass().getSimpleName()
        );
    }

    private CarStockStatus parseCarStockStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new DomainValidationException("Car status must not be blank.");
        }

        try {
            return CarStockStatus.valueOf(
                    rawStatus.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException("Unsupported car status: " + rawStatus);
        }
    }
}

