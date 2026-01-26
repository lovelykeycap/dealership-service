package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.exceptions.DomainValidationException;

public abstract class AbstractCarSearchPredicateHandler implements CarSearchPredicateHandler {
    protected boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected SlotVariant getRequiredSlot(FilterableCar car, SlotType slotType, String slotName) {
        SlotVariant slotVariant = car.getSlots().get(slotType);
        if (slotVariant == null) {
            throw new DomainValidationException(
                    "Car \"%s\" does not contain %s slot."
                            .formatted(car.getModel(), slotName)
            );
        }

        return slotVariant;
    }
}

