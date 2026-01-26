package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;

import java.util.function.Predicate;

public class BodyTypePredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (!hasText(criteria.bodyType())) {
            return currentPredicate;
        }

        return currentPredicate.and(filterableCar ->
                getRequiredSlot(filterableCar, SlotType.BODY, "body")
                        .getValue()
                        .equalsIgnoreCase(criteria.bodyType())
        );
    }
}

