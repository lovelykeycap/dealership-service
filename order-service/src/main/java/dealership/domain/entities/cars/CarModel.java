package dealership.domain.entities.cars;

import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.DomainValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CarModel {
    private UUID id;

    private String brand;

    private String model;

    private Money basePrice;

    private Map<SlotType, SlotVariant> baseSlots;

    private Set<SlotType> replaceableSlots;

    public CarModel(
            UUID id,
            String brand,
            String model,
            Money basePrice,
            Map<SlotType, SlotVariant> baseSlots,
            Set<SlotType> replaceableSlots
    ) {
        if (id == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (brand == null || brand.isBlank()) {
            throw new DomainValidationException("Car model brand must not be blank.");
        }
        if (model == null || model.isBlank()) {
            throw new DomainValidationException("Car model name must not be blank.");
        }
        if (basePrice == null) {
            throw new DomainValidationException("Car model base price must not be null.");
        }
        if (baseSlots == null) {
            throw new DomainValidationException("Car model base slots must not be null.");
        }
        if (replaceableSlots == null) {
            throw new DomainValidationException("Car model replaceable slots must not be null.");
        }

        this.id = id;
        this.brand = brand;
        this.model = model;
        this.basePrice = basePrice;
        this.baseSlots = new HashMap<>(baseSlots);
        this.replaceableSlots = new HashSet<>(replaceableSlots);
    }

    public UUID getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public Map<SlotType, SlotVariant> getBaseSlots() {
        return Map.copyOf(baseSlots);
    }

    public Set<SlotType> getReplaceableSlots() {
        return Set.copyOf(replaceableSlots);
    }

    public void rename(String model) {
        if (model == null || model.isBlank()) {
            throw new DomainValidationException("Car model name must not be blank.");
        }

        this.model = model;
    }

    public void changeBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new DomainValidationException("Car model brand must not be blank.");
        }

        this.brand = brand;
    }

    public void changeBasePrice(Money basePrice) {
        if (basePrice == null) {
            throw new DomainValidationException("Car model base price must not be null.");
        }

        this.basePrice = basePrice;
    }

    public void changeBaseSlots(Map<SlotType, SlotVariant> baseSlots) {
        if (baseSlots == null) {
            throw new DomainValidationException("Car model base slots must not be null.");
        }

        this.baseSlots = new HashMap<>(baseSlots);
    }

    public void changeReplaceableSlots(Set<SlotType> replaceableSlots) {
        if (replaceableSlots == null) {
            throw new DomainValidationException("Car model replaceable slots must not be null.");
        }

        this.replaceableSlots = new HashSet<>(replaceableSlots);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CarModel carModel)) {
            return false;
        }
        return Objects.equals(id, carModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
