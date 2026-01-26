package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.exceptions.DomainValidationException;

import java.util.function.Predicate;

public class PricePredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (criteria.minPrice() == null && criteria.maxPrice() == null) {
            return currentPredicate;
        }
        if (criteria.minPrice() != null
                && criteria.maxPrice() != null
                && criteria.minPrice().compareTo(criteria.maxPrice()) > 0) {
            throw new DomainValidationException("Minimum price must not exceed maximum price.");
        }

        return currentPredicate.and(filterableCar -> {
            boolean minMatches = criteria.minPrice() == null
                    || filterableCar.getPrice().amount().compareTo(criteria.minPrice()) >= 0;
            boolean maxMatches = criteria.maxPrice() == null
                    || filterableCar.getPrice().amount().compareTo(criteria.maxPrice()) <= 0;

            return minMatches && maxMatches;
        });
    }
}

