package dealership.storageservice.application.items.helpers;

import dealership.storageservice.application.items.operations.sparepart.UpdateSparePart;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SparePartChangeApplier {
    private final SlotTypeParser slotTypeParser;

    public SlotVariant applyChange(SlotVariant sparePart, UpdateSparePart.Change change) {
        if (change instanceof UpdateSparePart.ChangeSlotType changeSlotType) {
            return new SlotVariant(
                    sparePart.getId(),
                    slotTypeParser.parse(changeSlotType.slotType()),
                    sparePart.getValue(),
                    sparePart.getPrice(),
                    sparePart.getCompatibleCars()
            );
        }
        if (change instanceof UpdateSparePart.ChangeValue changeValue) {
            if (changeValue.value() == null || changeValue.value().isBlank()) {
                throw new DomainValidationException("Spare part value must not be blank.");
            }

            return new SlotVariant(
                    sparePart.getId(),
                    sparePart.getSlot(),
                    changeValue.value(),
                    sparePart.getPrice(),
                    sparePart.getCompatibleCars()
            );
        }
        if (change instanceof UpdateSparePart.ChangePrice changePrice) {
            if (changePrice.price() == null) {
                throw new DomainValidationException("Spare part price must not be null.");
            }

            return new SlotVariant(
                    sparePart.getId(),
                    sparePart.getSlot(),
                    sparePart.getValue(),
                    new Money(changePrice.price()),
                    sparePart.getCompatibleCars()
            );
        }
        if (change instanceof UpdateSparePart.ChangeCompatibleCarModelIds) {
            throw new DomainValidationException(
                    "ChangeCompatibleCarModelIds must be handled in SparePartService before applying spare part changes."
            );
        }

        throw new DomainValidationException(
                "Unsupported spare part change type: " + change.getClass().getSimpleName()
        );
    }
}

