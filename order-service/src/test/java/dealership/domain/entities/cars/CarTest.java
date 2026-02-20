package dealership.domain.entities.cars;

import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarTest {

    @Test
    void shouldCreateAvailableCarAndReturnConfigurationPrice() {
        CarModel carModel = createModelWithReplaceableWheels("50000.00", "1000.00");
        CarConfiguration configuration = new CarConfiguration(carModel);

        Car car = new Car(UUID.randomUUID(), configuration);

        assertEquals(CarStockStatus.AVAILABLE, car.getStatus());
        assertEquals(Money.of("50000.00"), car.getPrice());
    }

    @Test
    void shouldChangeCarStatus() {
        CarModel carModel = createModelWithReplaceableWheels("50000.00", "1000.00");
        Car car = new Car(UUID.randomUUID(), new CarConfiguration(carModel));

        car.changeStatus(CarStockStatus.RESERVED);

        assertEquals(CarStockStatus.RESERVED, car.getStatus());
    }

    private CarModel createModelWithReplaceableWheels(String basePrice, String wheelsPrice) {
        CarModel carModel = new CarModel(
                UUID.randomUUID(),
                "BMW",
                "X5",
                Money.of(basePrice),
                Map.of(),
                Set.of()
        );

        SlotVariant wheels = new SlotVariant(
                UUID.randomUUID(),
                SlotType.WHEELS,
                "Base wheels",
                Money.of(wheelsPrice),
                List.of(carModel)
        );

        carModel.changeBaseSlots(Map.of(SlotType.WHEELS, wheels));
        carModel.changeReplaceableSlots(Set.of(SlotType.WHEELS));
        return carModel;
    }
}
