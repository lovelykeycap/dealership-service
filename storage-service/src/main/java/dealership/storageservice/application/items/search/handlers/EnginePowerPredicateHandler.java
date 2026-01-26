package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.exceptions.DomainValidationException;

import java.util.function.Predicate;

public class EnginePowerPredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (criteria.minEnginePower() == null && criteria.maxEnginePower() == null) {
            return currentPredicate;
        }
        if (criteria.minEnginePower() != null
                && criteria.maxEnginePower() != null
                && criteria.minEnginePower() > criteria.maxEnginePower()) {
            throw new DomainValidationException("Minimum engine power must not exceed maximum engine power.");
        }

        return currentPredicate.and(filterableCar -> {
            String rawValue = getRequiredSlot(filterableCar, SlotType.ENGINE_POWER, "engine power").getValue();

            int enginePower;
            try {
                enginePower = Integer.parseInt(rawValue);
            } catch (NumberFormatException ex) {
                throw new DomainValidationException(
                        "Engine power value \"%s\" is not a valid integer."
                                .formatted(rawValue)
                );
            }

            boolean minMatches = criteria.minEnginePower() == null || enginePower >= criteria.minEnginePower();
            boolean maxMatches = criteria.maxEnginePower() == null || enginePower <= criteria.maxEnginePower();

            return minMatches && maxMatches;
        });
    }
}

