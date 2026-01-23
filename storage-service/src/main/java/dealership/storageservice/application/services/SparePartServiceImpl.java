package dealership.storageservice.application.services;

import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.SparePartService;
import dealership.storageservice.application.items.helpers.SlotTypeParser;
import dealership.storageservice.application.items.helpers.SparePartChangeApplier;
import dealership.storageservice.application.items.dtos.SlotRefDto;
import dealership.storageservice.application.items.operations.sparepart.AddSparePart;
import dealership.storageservice.application.items.operations.sparepart.CheckSparePart;
import dealership.storageservice.application.items.operations.sparepart.CheckSparePartList;
import dealership.storageservice.application.items.operations.sparepart.DeleteSparePart;
import dealership.storageservice.application.items.operations.sparepart.UpdateSparePart;
import dealership.storageservice.application.mappers.SparePartDtoMapper;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import dealership.storageservice.domain.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class SparePartServiceImpl implements SparePartService {
    private final PersistenceContext persistenceContext;
    private final SlotTypeParser slotTypeParser;
    private final SparePartDtoMapper sparePartDtoMapper;
    private final SparePartChangeApplier sparePartChangeApplier;

    @Override
    public AddSparePart.Response addSparePart(AddSparePart.Request request) {
        if (request == null) {
            throw new DomainValidationException("Add spare part request must not be null.");
        }
        if (request.value() == null || request.value().isBlank()) {
            throw new DomainValidationException("Spare part value must not be blank.");
        }
        if (request.price() == null) {
            throw new DomainValidationException("Spare part price must not be null.");
        }
        if (request.compatibleCarModelIds() == null) {
            throw new DomainValidationException("Compatible car model ids must not be null.");
        }

        List<CarModel> compatibleCars = resolveCompatibleCars(request.compatibleCarModelIds());

        UUID sparePartId = UUID.randomUUID();
        SlotVariant sparePart = new SlotVariant(
                sparePartId,
                slotTypeParser.parse(request.slotType()),
                request.value(),
                new Money(request.price()),
                compatibleCars
        );

        persistenceContext.getSlotVariantRepository().save(sparePart);
        return new AddSparePart.Response(sparePartId);
    }

    @Override
    public CheckSparePart.Response checkSparePart(CheckSparePart.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check spare part request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }

        SlotVariant sparePart = findSparePartById(request.sparePartId());
        return sparePartDtoMapper.toCheckSparePartResponse(sparePart);
    }

    @Override
    public CheckSparePartList.Response checkSparePartList(CheckSparePartList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check spare part list request must not be null.");
        }

        List<CheckSparePartList.SparePartDto> spareParts = persistenceContext.getSlotVariantRepository()
                .findAll()
                .stream()
                .map(sparePartDtoMapper::toSparePartDto)
                .toList();

        return new CheckSparePartList.Response(spareParts);
    }

    @Override
    public UpdateSparePart.Response updateSparePart(UpdateSparePart.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update spare part request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }
        if (request.changes() == null) {
            throw new DomainValidationException("Spare part changes must not be null.");
        }

        SlotVariant updatedSparePart = findSparePartById(request.sparePartId());

        for (UpdateSparePart.Change change : request.changes()) {
            if (change == null) {
                throw new DomainValidationException("Spare part change must not be null.");
            }

            if (change instanceof UpdateSparePart.ChangeCompatibleCarModelIds compatibleCarModelIds) {
                if (compatibleCarModelIds.compatibleCarModelIds() == null) {
                    throw new DomainValidationException("Compatible car model ids must not be null.");
                }

                updatedSparePart = new SlotVariant(
                        updatedSparePart.getId(),
                        updatedSparePart.getSlot(),
                        updatedSparePart.getValue(),
                        updatedSparePart.getPrice(),
                        resolveCompatibleCars(compatibleCarModelIds.compatibleCarModelIds())
                );
                continue;
            }

            updatedSparePart = sparePartChangeApplier.applyChange(updatedSparePart, change);
        }

        persistenceContext.getSlotVariantRepository().save(updatedSparePart);
        return new UpdateSparePart.Response(updatedSparePart.getId());
    }

    @Override
    public DeleteSparePart.Response deleteSparePart(DeleteSparePart.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete spare part request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }

        SlotVariant sparePart = findSparePartById(request.sparePartId());
        int stockQuantity = persistenceContext.getSlotVariantStockRepository().getQuantity(sparePart.getId());
        if (stockQuantity > 0) {
            throw new DomainValidationException(
                    "Cannot delete spare part \"%s\" while it is present in stock."
                            .formatted(sparePart.getId())
            );
        }

        persistenceContext.getSlotVariantRepository().deleteById(sparePart.getId());
        return new DeleteSparePart.Response(sparePart.getId());
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

    CarModel findCarModelById(UUID modelId) {
        return persistenceContext.getCarModelRepository()
                .findById(modelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Car model \"%s\" not found".formatted(modelId)
                        )
                );
    }

    private List<CarModel> resolveCompatibleCars(Set<UUID> compatibleCarModelIds) {
        return compatibleCarModelIds.stream()
                .map(this::findCarModelById)
                .toList();
    }
}

