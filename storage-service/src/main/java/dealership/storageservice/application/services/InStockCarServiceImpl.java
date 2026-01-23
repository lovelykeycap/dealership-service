package dealership.storageservice.application.services;

import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.InStockCarService;
import dealership.storageservice.application.items.helpers.CarChangeApplier;
import dealership.storageservice.application.items.dtos.SlotRefDto;
import dealership.storageservice.application.items.search.adapters.CarFilterableCarAdapter;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.application.items.search.builder.CarSearchPredicateBuilder;
import dealership.storageservice.application.mappers.InStockCarDtoMapper;
import dealership.storageservice.application.items.operations.car.*;
import dealership.storageservice.domain.configuration.CarConfiguration;
import dealership.storageservice.domain.entities.cars.Car;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import dealership.storageservice.domain.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@AllArgsConstructor
public class InStockCarServiceImpl implements InStockCarService {
    private final PersistenceContext persistenceContext;
    private final CarModelServiceImpl carModelService;
    private final SparePartServiceImpl sparePartService;
    private final SparePartStockServiceImpl sparePartStockService;
    private final CarSearchPredicateBuilder carSearchPredicateBuilder;
    private final InStockCarDtoMapper inStockCarDtoMapper;
    private final CarChangeApplier carChangeApplier;

    @Override
    public AddCar.Response addCar(AddCar.Request request) {
        if (request == null) {
            throw new DomainValidationException("Add car request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (request.selectedSlots() == null) {
            throw new DomainValidationException("Selected slots must not be null.");
        }

        CarConfiguration configuration = createConfiguration(request.modelId(), request.selectedSlots());

        UUID carId = UUID.randomUUID();
        Car car = new Car(carId, configuration);
        persistenceContext.getCarRepository().save(car);

        return new AddCar.Response(carId);
    }

    @Override
    public CheckInStockCar.Response checkInStockCar(CheckInStockCar.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check in-stock car request must not be null.");
        }
        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null.");
        }

        Car car = findCarById(request.carId());
        return inStockCarDtoMapper.toCheckInStockCarResponse(car);
    }

    @Override
    public CheckInStockCarList.Response checkInStockCarList(CheckInStockCarList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check in-stock car list request must not be null.");
        }

        Predicate<FilterableCar> predicate =
                carSearchPredicateBuilder.build(request.criteria());

        List<CheckInStockCarList.InStockCarDto> cars = persistenceContext.getCarRepository()
                .findAll()
                .stream()
                .filter(car -> predicate.test(new CarFilterableCarAdapter(car)))
                .map(inStockCarDtoMapper::toInStockCarDto)
                .toList();

        return new CheckInStockCarList.Response(cars);
    }

    @Override
    public UpdateCar.Response updateCar(UpdateCar.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update car request must not be null.");
        }
        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null.");
        }
        if (request.changes() == null) {
            throw new DomainValidationException("Car changes must not be null.");
        }

        Car car = findCarById(request.carId());

        for (UpdateCar.Change change : request.changes()) {
            if (change == null) {
                throw new DomainValidationException("Car change must not be null.");
            }

            if (change instanceof UpdateCar.ChangeSelectedSlots changeSelectedSlots) {
                if (changeSelectedSlots.selectedSlots() == null) {
                    throw new DomainValidationException("Selected slots must not be null.");
                }

                List<SlotVariant> selectedSlots =
                        sparePartStockService.resolveSparePartsInStock(changeSelectedSlots.selectedSlots());

                validateDuplicateSelectedSlots(selectedSlots);

                for (SlotVariant selectedSlot : selectedSlots) {
                    car.getConfiguration().setSlot(selectedSlot);
                }
                continue;
            }

            carChangeApplier.applyChange(car, change);
        }

        persistenceContext.getCarRepository().save(car);

        return new UpdateCar.Response(car.getId());
    }

    @Override
    public DeleteCar.Response deleteCar(DeleteCar.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete car request must not be null.");
        }
        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null.");
        }

        Car car = findCarById(request.carId());
        persistenceContext.getCarRepository().deleteById(car.getId());

        return new DeleteCar.Response();
    }

    Car findCarById(UUID carId) {
        return persistenceContext.getCarRepository()
                .findById(carId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Car \"%s\" not found".formatted(carId)
                        )
                );
    }

    CarConfiguration createConfiguration(UUID modelId, List<SlotRefDto> selectedSlots) {
        if (modelId == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (selectedSlots == null) {
            throw new DomainValidationException("Selected slots must not be null.");
        }

        CarModel carModel = carModelService.findCarModelById(modelId);
        List<SlotVariant> resolvedSlots = sparePartService.resolveSpareParts(selectedSlots);

        validateDuplicateSelectedSlots(resolvedSlots);

        CarConfiguration configuration = new CarConfiguration(carModel);
        for (SlotVariant selectedSlot : resolvedSlots) {
            configuration.setSlot(selectedSlot);
        }

        return configuration;
    }

    private void validateDuplicateSelectedSlots(List<SlotVariant> selectedSlots) {
        Set<SlotType> selectedSlotTypes = new HashSet<>();

        for (SlotVariant selectedSlot : selectedSlots) {
            if (!selectedSlotTypes.add(selectedSlot.getSlot())) {
                throw new DomainValidationException(
                        "Duplicate selected slot type \"%s\" is not allowed."
                                .formatted(selectedSlot.getSlot())
                );
            }
        }
    }
}

