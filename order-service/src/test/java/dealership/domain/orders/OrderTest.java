package dealership.domain.orders;

import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void shouldCreatePlacedInStockOrder() {
        Car car = createCar();
        Order order = new Order(
                UUID.randomUUID(),
                false,
                UUID.randomUUID(),
                UUID.randomUUID(),
                car.getConfiguration(),
                car
        );

        assertEquals("Placed", order.getStatus());
        assertEquals(car, order.getCar());
    }

    @Test
    void shouldCompleteInStockOrderThroughMainFlow() {
        Car car = createCar();
        Order order = new Order(
                UUID.randomUUID(),
                false,
                UUID.randomUUID(),
                UUID.randomUUID(),
                car.getConfiguration(),
                car
        );

        order.markApproved();
        order.markWaitingForPayment();
        order.markPaid();
        order.markReadyForPickup();
        order.markCompleted();

        assertEquals("Completed", order.getStatus());
    }

    @Test
    void shouldRequireDeliveryStepForConfiguredOrder() {
        CarConfiguration configuration = createConfiguration();
        Order order = new Order(
                UUID.randomUUID(),
                true,
                UUID.randomUUID(),
                UUID.randomUUID(),
                configuration,
                null
        );

        order.markApproved();
        order.markWaitingForPayment();
        order.markPaid();
        order.markWaitingForDelivery();
        order.markReadyForPickup();

        assertEquals("Ready for pickup", order.getStatus());
    }

    @Test
    void shouldCancelOrderFromPlacedState() {
        Car car = createCar();
        Order order = new Order(
                UUID.randomUUID(),
                false,
                UUID.randomUUID(),
                UUID.randomUUID(),
                car.getConfiguration(),
                car
        );

        order.markCancelled();

        assertEquals("Cancelled", order.getStatus());
    }

    @Test
    void shouldThrowWhenTryingToCompleteOrderTooEarly() {
        Car car = createCar();
        Order order = new Order(
                UUID.randomUUID(),
                false,
                UUID.randomUUID(),
                UUID.randomUUID(),
                car.getConfiguration(),
                car
        );

        assertThrows(IllegalStateException.class, order::markCompleted);
    }

    private Car createCar() {
        return new Car(UUID.randomUUID(), createConfiguration());
    }

    private CarConfiguration createConfiguration() {
        CarModel carModel = new CarModel(
                UUID.randomUUID(),
                "BMW",
                "X5",
                Money.of("50000.00"),
                Map.of(),
                Set.of()
        );

        SlotVariant wheels = new SlotVariant(
                UUID.randomUUID(),
                SlotType.WHEELS,
                "Base wheels",
                Money.of("1000.00"),
                List.of(carModel)
        );

        carModel.changeBaseSlots(Map.of(SlotType.WHEELS, wheels));
        carModel.changeReplaceableSlots(Set.of(SlotType.WHEELS));
        return new CarConfiguration(carModel);
    }
}
