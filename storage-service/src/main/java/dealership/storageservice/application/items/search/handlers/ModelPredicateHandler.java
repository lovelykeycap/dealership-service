package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;

import java.util.function.Predicate;

public class ModelPredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (!hasText(criteria.model())) {
            return currentPredicate;
        }

        return currentPredicate.and(
                filterableCar -> filterableCar.getModel().equalsIgnoreCase(criteria.model())
        );
    }
}

