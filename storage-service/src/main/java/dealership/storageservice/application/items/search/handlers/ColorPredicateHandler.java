package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;

import java.util.function.Predicate;

public class ColorPredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (!hasText(criteria.color())) {
            return currentPredicate;
        }

        return currentPredicate.and(filterableCar ->
                getRequiredSlot(filterableCar, SlotType.COLOR, "color")
                        .getValue()
                        .equalsIgnoreCase(criteria.color())
        );
    }
}

