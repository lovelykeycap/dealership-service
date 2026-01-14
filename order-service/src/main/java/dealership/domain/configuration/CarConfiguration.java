package dealership.domain.configuration;

import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.IncompatibleComponentException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CarConfiguration {
    private final CarModel model;
    private final Map<SlotType, SlotVariant> selectedSlots;

    public CarConfiguration(CarModel model) {

        if (model == null) {
            throw new DomainValidationException("Car model must not be null.");
        }
        if (model.getBaseSlots() == null) {
            throw new DomainValidationException("Car model base slots must not be null.");
        }
        if (model.getReplaceableSlots() == null) {
            throw new DomainValidationException("Car model replaceable slots must not be null.");
        }

        this.model = model;
        this.selectedSlots = new HashMap<>(model.getBaseSlots());

        for (Map.Entry<SlotType, SlotVariant> entry : selectedSlots.entrySet()) {
            validateSlotEntry(entry);
        }
        for (SlotType slotType : model.getBaseSlots().keySet()) {
            validateBaseSlotPresence(slotType);
        }
        for (SlotType slotType : model.getReplaceableSlots()) {
            validateReplaceableSlot(slotType);
        }
    }


    public void setSlot(SlotVariant newVariant) {

        if (newVariant == null) {
            throw new DomainValidationException("Component must not be null.");
        }
        if (!model.getReplaceableSlots().contains(newVariant.getSlot())) {
            throw new DomainValidationException(
                    "The slot \"%s\" is not replaceable for model \"%s\"."
                            .formatted(newVariant.getSlot(), model.getModel())
            );
        }

        if (selectedSlots.containsKey(newVariant.getSlot())) {
            if (newVariant.getCompatibleCars().contains(model)) {
                selectedSlots.put(newVariant.getSlot(), newVariant);
                return;
            }

            throw new IncompatibleComponentException(
                    "The model \"%s\" is not compatible with the component \"%s\"."
                            .formatted(model.getModel(), newVariant.getValue())
            );
        }

        throw new DomainValidationException(
                "The model \"%s\" does not contain the required slot \"%s\"."
                        .formatted(model.getModel(), newVariant.getSlot())
        );
    }

        public Money calculatePrice() {
        BigDecimal delta = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (Map.Entry<SlotType, SlotVariant> entry : selectedSlots.entrySet()) {
            SlotType slotType = entry.getKey();
            SlotVariant selectedVariant = entry.getValue();

            SlotVariant baseVariant = model.getBaseSlots().get(slotType);

            BigDecimal difference = selectedVariant.getPrice().amount()
                    .subtract(baseVariant.getPrice().amount())
                    .setScale(2, RoundingMode.HALF_UP);

            delta = delta.add(difference).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = model.getBasePrice().amount()
                .add(delta)
                .setScale(2, RoundingMode.HALF_UP);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Total configuration price must not be negative.");
        }

        return new Money(totalAmount);
    }
    private void validateSlotEntry(Map.Entry<SlotType, SlotVariant> entry) {
        SlotType slotType = entry.getKey();
        SlotVariant variant = entry.getValue();

        if (slotType == null) {
            throw new DomainValidationException("Slot type must not be null.");
        }
        if (variant == null) {
            throw new DomainValidationException("Variant must not be null.");
        }
        if (!model.getBaseSlots().containsKey(slotType)) {
            throw new DomainValidationException(
                    "Slot \"%s\" is not supported by model \"%s\"."
                            .formatted(slotType, model.getModel())
            );
        }
        if (variant.getSlot() != slotType) {
            throw new DomainValidationException("Slot mismatch.");
        }
        if (variant.getCompatibleCars() != null
                && !variant.getCompatibleCars().isEmpty()
                && !variant.getCompatibleCars().contains(model)) {
            throw new IncompatibleComponentException("Incompatible component.");
        }
    }

    private void validateBaseSlotPresence(SlotType slotType) {
        if (!selectedSlots.containsKey(slotType)) {
            throw new DomainValidationException(
                    "Required base slot \"%s\" is missing in selected slots."
                            .formatted(slotType)
            );
        }
    }

    private void validateReplaceableSlot(SlotType slotType) {
        if (slotType == null) {
            throw new DomainValidationException("Replaceable slot type must not be null.");
        }
        if (!selectedSlots.containsKey(slotType)) {
            throw new DomainValidationException(
                    "Replaceable slot \"%s\" is not present in selected slots."
                            .formatted(slotType)
            );
        }
    }
}
