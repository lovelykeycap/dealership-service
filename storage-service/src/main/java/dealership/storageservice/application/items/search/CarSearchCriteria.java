package dealership.storageservice.application.items.search;

import java.math.BigDecimal;

public record CarSearchCriteria(
        String brand,
        String model,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String bodyType,
        String fuelType,
        Integer minEnginePower,
        Integer maxEnginePower,
        BigDecimal minEngineVolume,
        BigDecimal maxEngineVolume,
        String transmissionType,
        String driveType,
        String color
) {
    public static CarSearchCriteria empty() {
        return new CarSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}


