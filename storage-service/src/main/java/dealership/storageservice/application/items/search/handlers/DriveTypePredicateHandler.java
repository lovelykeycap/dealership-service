package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;

import java.util.function.Predicate;

public class DriveTypePredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (!hasText(criteria.driveType())) {
            return currentPredicate;
        }

        return currentPredicate.and(filterableCar ->
                getRequiredSlot(filterableCar, SlotType.DRIVE, "drive")
                        .getValue()
                        .equalsIgnoreCase(criteria.driveType())
        );
    }
}

