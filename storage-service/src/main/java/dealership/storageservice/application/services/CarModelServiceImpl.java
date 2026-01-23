package dealership.storageservice.application.services;

import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.CarModelService;
import dealership.storageservice.application.items.helpers.CarModelChangeApplier;
import dealership.storageservice.application.items.dtos.SlotRefDto;
import dealership.storageservice.application.items.operations.carmodel.AddCarModel;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModel;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModelList;
import dealership.storageservice.application.items.operations.carmodel.DeleteCarModel;
import dealership.storageservice.application.items.operations.carmodel.UpdateCarModel;
import dealership.storageservice.application.items.search.adapters.CarModelFilterableCarAdapter;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.application.items.search.builder.CarSearchPredicateBuilder;
import dealership.storageservice.application.mappers.CarModelDtoMapper;
import dealership.storageservice.application.mappers.CarModelInputMapper;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import dealership.storageservice.domain.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@AllArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private final PersistenceContext persistenceContext;
    private final CarModelDtoMapper carModelDtoMapper;
    private final CarModelInputMapper carModelInputMapper;
    private final CarSearchPredicateBuilder carSearchPredicateBuilder;
    private final CarModelChangeApplier carModelChangeApplier;

    @Override
    public AddCarModel.Response addCarModel(AddCarModel.Request request) {

        if (request == null) {
            throw new DomainValidationException("Add car model request must not be null.");
        }
        if (request.baseSlots() == null) {
            throw new DomainValidationException("Base slots must not be null.");
        }
        if (request.replaceableSlots() == null) {
            throw new DomainValidationException("Replaceable slots must not be null.");
        }

        Map<SlotType, SlotVariant> baseSlots =
                carModelInputMapper.toBaseSlots(
                        this.resolveSpareParts(request.baseSlots())
                );
        Set<SlotType> replaceableSlots =
                carModelInputMapper.toReplaceableSlots(request.replaceableSlots());

        UUID modelId = UUID.randomUUID();
        CarModel newModel = new CarModel(
                modelId,
                request.brand(),
                request.name(),
                new Money(request.price()),
                baseSlots,
                replaceableSlots
        );

        persistenceContext.getCarModelRepository().save(newModel);

        return new AddCarModel.Response(modelId);
    }

    @Override
    public CheckCarModel.Response checkCarModel(CheckCarModel.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check car model request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }

        CarModel carModel = findCarModelById(request.modelId());
        return carModelDtoMapper.toCheckCarModelResponse(carModel);
    }

    @Override
    public CheckCarModelList.Response checkCarModelList(CheckCarModelList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check car model list request must not be null.");
        }

        Predicate<FilterableCar> predicate =
                carSearchPredicateBuilder.build(request.criteria());

        List<CheckCarModelList.CarModelDto> carModels = persistenceContext.getCarModelRepository()
                .findAll()
                .stream()
                .filter(model -> predicate.test(new CarModelFilterableCarAdapter(model)))
                .map(carModelDtoMapper::toCarModelDto)
                .toList();

        return new CheckCarModelList.Response(carModels);
    }

    @Override
    public UpdateCarModel.Response updateCarModel(UpdateCarModel.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update car model request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (request.changes() == null) {
            throw new DomainValidationException("Car model changes must not be null.");
        }

        CarModel carModel = findCarModelById(request.modelId());

        for (UpdateCarModel.Change change : request.changes()) {
            if (change == null) {
                throw new DomainValidationException("Car model change must not be null.");
            }

            if (change instanceof UpdateCarModel.ChangeBaseSlots(List<SlotRefDto> baseSlots)) {
                if (baseSlots == null) {
                    throw new DomainValidationException("Base slots must not be null.");
                }

                carModel.changeBaseSlots(
                        carModelInputMapper.toBaseSlots(
                                this.resolveSpareParts(baseSlots)
                        )
                );
                continue;
            }

            carModelChangeApplier.applyChange(carModel, change);
        }

        persistenceContext.getCarModelRepository().save(carModel);

        return new UpdateCarModel.Response(carModel.getId());
    }

    @Override
    public DeleteCarModel.Response deleteCarModel(DeleteCarModel.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete car model request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }

        CarModel carModel = findCarModelById(request.modelId());
        persistenceContext.getCarModelRepository().deleteById(carModel.getId());

        return new DeleteCarModel.Response(carModel.getId());
    }

    CarModel findCarModelById(UUID modelId) {
        return persistenceContext.getCarModelRepository()
                .findById(modelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Car model \"%s\" not found".formatted(modelId)
                        )
                );
    }

    SlotVariant findSparePartById(UUID sparePartId) {
        return persistenceContext.getSlotVariantRepository()
                .findById(sparePartId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Spare part \"%s\" not found".formatted(sparePartId)
                        )
                );
    }

    List<SlotVariant> resolveSpareParts(List<SlotRefDto> slotRefs) {
        return slotRefs.stream()
                .map(SlotRefDto::id)
                .map(this::findSparePartById)
                .toList();
    }
}

