package dealership.storageservice.application.items.helpers;

import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.exceptions.DomainValidationException;

public class SlotTypeParser {
    public SlotType parse(String rawSlotType) {
        if (rawSlotType == null || rawSlotType.isBlank()) {
            throw new DomainValidationException("Slot type must not be blank.");
        }

        try {
            return SlotType.valueOf(
                    rawSlotType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException("Unsupported slot type: " + rawSlotType);
        }
    }
}

