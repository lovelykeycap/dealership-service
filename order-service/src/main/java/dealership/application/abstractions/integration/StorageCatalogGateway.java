package dealership.application.abstractions.integration;

import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.slots.SlotVariant;

import java.util.UUID;

public interface StorageCatalogGateway {
    Car getInStockCar(UUID carId);

    CarModel getCarModel(UUID carModelId);

    SlotVariant getSparePartAsSlotVariant(UUID sparePartId, CarModel currentModel);
}
