package dealership.storageservice.application.items.search.adapters;

import dealership.storageservice.domain.entities.cars.Car;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class CarFilterableCarAdapter implements FilterableCar {

    private final Car car;

    @Override
    public String getBrand() {
        return car.getConfiguration().getModel().getBrand();
    }

    @Override
    public String getModel() {
        return car.getConfiguration().getModel().getModel();
    }

    @Override
    public Money getPrice() {
        return car.getPrice();
    }

    @Override
    public Map<SlotType, SlotVariant> getSlots() {
        return car.getConfiguration().getSelectedSlots();
    }
}

