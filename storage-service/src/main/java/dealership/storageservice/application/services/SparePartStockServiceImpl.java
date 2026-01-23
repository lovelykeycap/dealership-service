package dealership.storageservice.application.services;

import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.SparePartStockService;
import dealership.storageservice.application.items.dtos.SlotRefDto;
import dealership.storageservice.application.items.operations.sparepartstock.AddSparePartStock;
import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStock;
import dealership.storageservice.application.items.operations.sparepartstock.CheckSparePartStockList;
import dealership.storageservice.application.items.operations.sparepartstock.RemoveSparePartStock;
import dealership.storageservice.application.mappers.SparePartStockDtoMapper;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SparePartStockServiceImpl implements SparePartStockService {
    private final PersistenceContext persistenceContext;
    private final SparePartServiceImpl sparePartService;
    private final SparePartStockDtoMapper sparePartStockDtoMapper;

    @Override
    public AddSparePartStock.Response addSparePartStock(AddSparePartStock.Request request) {
        if (request == null) {
            throw new DomainValidationException("Add spare part stock request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }
        if (request.quantity() <= 0) {
            throw new DomainValidationException("Stock quantity must be greater than zero.");
        }

        SlotVariant sparePart = sparePartService.findSparePartById(request.sparePartId());
        persistenceContext.getSlotVariantStockRepository().addQuantity(sparePart.getId(), request.quantity());

        return new AddSparePartStock.Response(sparePart.getId());
    }

    @Override
    public CheckSparePartStock.Response checkSparePartStock(CheckSparePartStock.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check spare part stock request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }

        SlotVariant sparePart = sparePartService.findSparePartById(request.sparePartId());
        int quantity = persistenceContext.getSlotVariantStockRepository().getQuantity(sparePart.getId());

        return sparePartStockDtoMapper.toCheckSparePartStockResponse(sparePart, quantity);
    }

    @Override
    public CheckSparePartStockList.Response checkSparePartStockList(CheckSparePartStockList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check spare part stock list request must not be null.");
        }

        List<CheckSparePartStockList.SparePartStockDto> sparePartStocks = persistenceContext.getSlotVariantRepository()
                .findAll()
                .stream()
                .filter(slotVariant -> persistenceContext.getSlotVariantStockRepository()
                        .getQuantity(slotVariant.getId()) > 0)
                .map(slotVariant -> sparePartStockDtoMapper.toSparePartStockDto(
                        slotVariant,
                        persistenceContext.getSlotVariantStockRepository().getQuantity(slotVariant.getId())
                ))
                .toList();

        return new CheckSparePartStockList.Response(sparePartStocks);
    }

    @Override
    public RemoveSparePartStock.Response removeSparePartStock(RemoveSparePartStock.Request request) {
        if (request == null) {
            throw new DomainValidationException("Remove spare part stock request must not be null.");
        }
        if (request.sparePartId() == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }
        if (request.quantity() <= 0) {
            throw new DomainValidationException("Stock quantity must be greater than zero.");
        }

        SlotVariant sparePart = sparePartService.findSparePartById(request.sparePartId());
        if (!persistenceContext.getSlotVariantStockRepository().hasAtLeast(sparePart.getId(), request.quantity())) {
            throw new DomainValidationException(
                    "Not enough spare part stock for \"%s\"."
                            .formatted(sparePart.getId())
            );
        }

        persistenceContext.getSlotVariantStockRepository().removeQuantity(sparePart.getId(), request.quantity());
        return new RemoveSparePartStock.Response(sparePart.getId());
    }

    SlotVariant findSparePartInStockById(UUID sparePartId) {
        SlotVariant sparePart = sparePartService.findSparePartById(sparePartId);
        if (!persistenceContext.getSlotVariantStockRepository().hasAtLeast(sparePart.getId(), 1)) {
            throw new DomainValidationException(
                    "Spare part \"%s\" is not available in stock."
                            .formatted(sparePartId)
            );
        }

        return sparePart;
    }

    List<SlotVariant> resolveSparePartsInStock(List<SlotRefDto> slotRefs) {
        return slotRefs.stream()
                .map(SlotRefDto::id)
                .map(this::findSparePartInStockById)
                .toList();
    }
}

