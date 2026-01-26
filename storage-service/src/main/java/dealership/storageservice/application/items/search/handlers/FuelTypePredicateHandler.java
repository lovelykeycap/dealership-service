package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;

import java.util.function.Predicate;

public class FuelTypePredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (!hasText(criteria.fuelType())) {
            return currentPredicate;
        }

        return currentPredicate.and(filterableCar ->
                getRequiredSlot(filterableCar, SlotType.FUEL_TYPE, "fuel type")
                        .getValue()
                        .equalsIgnoreCase(criteria.fuelType())
        );
    }
}

