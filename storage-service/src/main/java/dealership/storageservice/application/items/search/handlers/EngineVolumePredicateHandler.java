package dealership.storageservice.application.items.search.handlers;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class EngineVolumePredicateHandler extends AbstractCarSearchPredicateHandler {
    @Override
    public Predicate<FilterableCar> apply(CarSearchCriteria criteria, Predicate<FilterableCar> currentPredicate) {
        if (criteria.minEngineVolume() == null && criteria.maxEngineVolume() == null) {
            return currentPredicate;
        }
        if (criteria.minEngineVolume() != null
                && criteria.maxEngineVolume() != null
                && criteria.minEngineVolume().compareTo(criteria.maxEngineVolume()) > 0) {
            throw new DomainValidationException("Minimum engine volume must not exceed maximum engine volume.");
        }

        return currentPredicate.and(filterableCar -> {
            String rawValue = getRequiredSlot(filterableCar, SlotType.ENGINE_VOLUME, "engine volume").getValue();

            BigDecimal engineVolume;
            try {
                engineVolume = new BigDecimal(rawValue);
            } catch (NumberFormatException ex) {
                throw new DomainValidationException(
                        "Engine volume value \"%s\" is not a valid decimal."
                                .formatted(rawValue)
                );
            }

            boolean minMatches = criteria.minEngineVolume() == null
                    || engineVolume.compareTo(criteria.minEngineVolume()) >= 0;
            boolean maxMatches = criteria.maxEngineVolume() == null
                    || engineVolume.compareTo(criteria.maxEngineVolume()) <= 0;

            return minMatches && maxMatches;
        });
    }
}

