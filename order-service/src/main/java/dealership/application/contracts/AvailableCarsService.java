package dealership.application.contracts;

import dealership.application.items.operations.availablecars.CheckAvailableCar;
import dealership.application.items.operations.availablecars.CheckAvailableCars;

public interface AvailableCarsService {

    CheckAvailableCars.Response checkAvailableCars(CheckAvailableCars.Request request);

    CheckAvailableCar.Response checkAvailableCar(CheckAvailableCar.Request request);
}
