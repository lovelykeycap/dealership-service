package dealership.storageservice.application.items.search.adapters;

import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class CarModelFilterableCarAdapter implements FilterableCar {

    private final CarModel model;

    @Override
    public String getBrand() {
        return model.getBrand();
    }

    @Override
    public String getModel() {
        return model.getModel();
    }

    @Override
    public Money getPrice() {
        return model.getBasePrice();
    }

    @Override
    public Map<SlotType, SlotVariant> getSlots() {
        return model.getBaseSlots();
    }
}

