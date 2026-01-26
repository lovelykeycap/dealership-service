package dealership.storageservice.application.items.search.builder;

import dealership.storageservice.application.items.search.CarSearchCriteria;
import dealership.storageservice.application.items.search.adapters.FilterableCar;
import dealership.storageservice.application.items.search.handlers.CarSearchPredicateHandler;

import java.util.List;
import java.util.function.Predicate;

public class CarSearchPredicateBuilder {
    private final List<CarSearchPredicateHandler> handlers;

    public CarSearchPredicateBuilder(List<CarSearchPredicateHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public Predicate<FilterableCar> build(CarSearchCriteria criteria) {
        CarSearchCriteria safeCriteria = criteria == null ? CarSearchCriteria.empty() : criteria;
        Predicate<FilterableCar> predicate = filterableCar -> true;

        for (CarSearchPredicateHandler handler : handlers) {
            predicate = handler.apply(safeCriteria, predicate);
        }

        return predicate;
    }
}

