package dealership.storageservice.application.items.operations.car;

import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.application.items.search.CarSearchCriteria;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CheckInStockCarList {

    private CheckInStockCarList() { }

    public record Request(CarSearchCriteria criteria) {
        public Request(CarSearchCriteria criteria) {
            if (criteria == null) {
                this.criteria = CarSearchCriteria.empty();
                return;
            }

            this.criteria = criteria;
        }
    }

    public record Response(List<InStockCarDto> cars) {}

    public record InStockCarDto(
            UUID carId,
            UUID modelId,
            String brand,
            String model,
            BigDecimal price,
            String status,
            List<SlotDto> selectedSlots
    ) {}
}


