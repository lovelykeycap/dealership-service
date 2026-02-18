package dealership.application.services;

import dealership.application.abstractions.integration.StorageAvailableCarsGateway;
import dealership.application.contracts.AvailableCarsService;
import dealership.application.items.operations.availablecars.CheckAvailableCar;
import dealership.application.items.operations.availablecars.CheckAvailableCars;
import dealership.domain.exceptions.DomainValidationException;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@AllArgsConstructor
public class AvailableCarsServiceImpl implements AvailableCarsService {

    private final StorageAvailableCarsGateway storageAvailableCarsGateway;

    @Override
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckAvailableCars.Response checkAvailableCars(CheckAvailableCars.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check available cars request must not be null.");
        }

        List<CheckAvailableCar.Response> cars = storageAvailableCarsGateway.getAvailableCars()
                .stream()
                .map(this::toResponse)
                .toList();

        return new CheckAvailableCars.Response(cars);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckAvailableCar.Response checkAvailableCar(CheckAvailableCar.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check available car request must not be null.");
        }
        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null.");
        }

        return toResponse(storageAvailableCarsGateway.getAvailableCarById(request.carId()));
    }

    private CheckAvailableCar.Response toResponse(StorageAvailableCarsGateway.AvailableCarData data) {
        return new CheckAvailableCar.Response(
                data.carId(),
                data.modelId(),
                data.brand(),
                data.model(),
                data.price(),
                data.status(),
                data.selectedSlots()
        );
    }
}
