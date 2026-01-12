package dealership.domain.entities.slots;

import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SlotVariant {
    private final UUID id;

    private final SlotType slot;

    private final String value;

    private final Money price;

    private final List<CarModel> compatibleCars;
}
