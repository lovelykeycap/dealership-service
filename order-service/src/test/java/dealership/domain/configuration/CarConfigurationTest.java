package dealership.domain.configuration;

import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.IncompatibleComponentException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarConfigurationTest {

    @Test
    void shouldInitializeSelectedSlotsFromBaseSlots() {
        CarModel carModel = createModelWithReplaceableWheels("50000.00", "1000.00");

        CarConfiguration configuration = new CarConfiguration(carModel);

        assertEquals(carModel.getBaseSlots(), configuration.getSelectedSlots());
        assertEquals(Money.of("50000.00"), configuration.calculatePrice());
    }

    @Test
    void shouldReplaceSlotWhenComponentIsCompatibleAndReplaceable() {
        CarModel carModel = createModelWithReplaceableWheels("50000.00", "1000.00");
        CarConfiguration configuration = new CarConfiguration(carModel);
        SlotVariant sportWheels = new SlotVariant(
                UUID.randomUUID(),
                SlotType.WHEELS,
                "Sport wheels",
                Money.of("1500.00"),
                List.of(carModel)
        );

        configuration.setSlot(sportWheels);

        assertEquals(sportWheels, configuration.getSelectedSlots().get(SlotType.WHEELS));
        assertEquals(Money.of("50500.00"), configuration.calculatePrice());
    }

    @Test
    void shouldThrowExceptionWhenReplacingNonReplaceableSlot() {
        CarModel carModel = createModelWithTransmissionOnly("50000.00", "2000.00");
        CarConfiguration configuration = new CarConfiguration(carModel);
        SlotVariant automaticTransmission = new SlotVariant(
                UUID.randomUUID(),
                SlotType.TRANSMISSION,
                "Automatic transmission",
                Money.of("2500.00"),
                List.of(carModel)
        );

        assertThrows(DomainValidationException.class, () -> configuration.setSlot(automaticTransmission));
    }

    @Test
    void shouldThrowExceptionWhenReplacingSlotWithIncompatibleComponent() {
        CarModel carModel = createModelWithReplaceableWheels("50000.00", "1000.00");
        CarConfiguration configuration = new CarConfiguration(carModel);
        SlotVariant incompatibleWheels = new SlotVariant(
                UUID.randomUUID(),
                SlotType.WHEELS,
                "Incompatible wheels",
                Money.of("1200.00"),
                List.of()
        );

        assertThrows(IncompatibleComponentException.class, () -> configuration.setSlot(incompatibleWheels));
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

    private CarModel createModelWithTransmissionOnly(String basePrice, String transmissionPrice) {
        CarModel carModel = new CarModel(
                UUID.randomUUID(),
                "BMW",
                "X5",
                Money.of(basePrice),
                Map.of(),
                Set.of()
        );

        SlotVariant transmission = new SlotVariant(
                UUID.randomUUID(),
                SlotType.TRANSMISSION,
                "Manual transmission",
                Money.of(transmissionPrice),
                List.of(carModel)
        );

        carModel.changeBaseSlots(Map.of(SlotType.TRANSMISSION, transmission));
        carModel.changeReplaceableSlots(Set.of());
        return carModel;
    }
}
