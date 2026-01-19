package dealership.storageservice.application.contracts;

import dealership.storageservice.application.items.operations.car.AddCar;
import dealership.storageservice.application.items.operations.car.CheckInStockCar;
import dealership.storageservice.application.items.operations.car.CheckInStockCarList;
import dealership.storageservice.application.items.operations.car.DeleteCar;
import dealership.storageservice.application.items.operations.car.UpdateCar;

public interface InStockCarService {

    AddCar.Response addCar(AddCar.Request request);

    CheckInStockCar.Response checkInStockCar(CheckInStockCar.Request request);

    CheckInStockCarList.Response checkInStockCarList(CheckInStockCarList.Request request);

    UpdateCar.Response updateCar(UpdateCar.Request request);

    DeleteCar.Response deleteCar(DeleteCar.Request request);
}
