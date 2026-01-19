package dealership.storageservice.application.contracts;

import dealership.storageservice.application.items.operations.carmodel.AddCarModel;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModel;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModelList;
import dealership.storageservice.application.items.operations.carmodel.DeleteCarModel;
import dealership.storageservice.application.items.operations.carmodel.UpdateCarModel;

public interface CarModelService {

    AddCarModel.Response addCarModel(AddCarModel.Request request);

    CheckCarModel.Response checkCarModel(CheckCarModel.Request request);

    CheckCarModelList.Response checkCarModelList(CheckCarModelList.Request request);

    UpdateCarModel.Response updateCarModel(UpdateCarModel.Request request);

    DeleteCarModel.Response deleteCarModel(DeleteCarModel.Request request);
}
