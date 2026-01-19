package dealership.storageservice.application.items.operations.carmodel;

import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.application.items.search.CarSearchCriteria;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CheckCarModelList {

    private CheckCarModelList() { }

    public record Request(CarSearchCriteria criteria) {
        public Request(CarSearchCriteria criteria) {
            if (criteria == null) {
                this.criteria = CarSearchCriteria.empty();
                return;
            }

            this.criteria = criteria;
        }
    }

    public record Response(List<CarModelDto> carModels) {}

    public record CarModelDto(
            UUID modelId,
            String brand,
            String name,
            BigDecimal price,
            List<SlotDto> baseSlots,
            Set<String> replaceableSlots
    ) {}
}


