package dealership.domain.entities.cars;

import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarModelTest {

    @Test
    void shouldCopyCollectionsAndExposeThemAsReadOnly() {
        Map<SlotType, SlotVariant> baseSlots = new HashMap<>();
        Set<SlotType> replaceableSlots = new HashSet<>();

        SlotVariant wheels = new SlotVariant(
                UUID.randomUUID(),
                SlotType.WHEELS,
                "Base wheels",
                Money.of("1000.00"),
                List.of()
        );

        baseSlots.put(SlotType.WHEELS, wheels);
        replaceableSlots.add(SlotType.WHEELS);

        CarModel carModel = new CarModel(
                UUID.randomUUID(),
                "BMW",
                "X5",
                Money.of("50000.00"),
                baseSlots,
                replaceableSlots
        );

        baseSlots.clear();
        replaceableSlots.clear();

        assertEquals(1, carModel.getBaseSlots().size());
        assertEquals(1, carModel.getReplaceableSlots().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> carModel.getBaseSlots().put(SlotType.INTERIOR, wheels)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> carModel.getReplaceableSlots().add(SlotType.INTERIOR)
        );
    }

    @Test
    void shouldRenameAndChangeBrandAndBasePrice() {
        CarModel carModel = new CarModel(
                UUID.randomUUID(),
                "BMW",
                "X5",
                Money.of("50000.00"),
                Map.of(),
                Set.of()
        );

        carModel.rename("X6");
        carModel.changeBrand("Audi");
        carModel.changeBasePrice(Money.of("65000.00"));

        assertEquals("X6", carModel.getModel());
        assertEquals("Audi", carModel.getBrand());
        assertEquals(Money.of("65000.00"), carModel.getBasePrice());
    }
}
