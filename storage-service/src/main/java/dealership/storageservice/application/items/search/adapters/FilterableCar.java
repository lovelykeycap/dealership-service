package dealership.storageservice.application.items.search.adapters;

import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;

import java.util.Map;

public interface FilterableCar {

    String getBrand();

    String getModel();

    Money getPrice();

    Map<SlotType, SlotVariant> getSlots();
}

