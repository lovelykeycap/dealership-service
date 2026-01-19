package dealership.storageservice.application.contracts;

import dealership.storageservice.application.items.operations.sparepart.AddSparePart;
import dealership.storageservice.application.items.operations.sparepart.CheckSparePart;
import dealership.storageservice.application.items.operations.sparepart.CheckSparePartList;
import dealership.storageservice.application.items.operations.sparepart.DeleteSparePart;
import dealership.storageservice.application.items.operations.sparepart.UpdateSparePart;

public interface SparePartService {

    AddSparePart.Response addSparePart(AddSparePart.Request request);

    CheckSparePart.Response checkSparePart(CheckSparePart.Request request);

    CheckSparePartList.Response checkSparePartList(CheckSparePartList.Request request);

    UpdateSparePart.Response updateSparePart(UpdateSparePart.Request request);

    DeleteSparePart.Response deleteSparePart(DeleteSparePart.Request request);
}
