package dealership.application.contracts;

import dealership.application.items.operations.testdrive.AddCarModelToTestDriveList;
import dealership.application.items.operations.testdrive.CheckTestDrive;
import dealership.application.items.operations.testdrive.CheckTestDriveList;
import dealership.application.items.operations.testdrive.CreateTestDrive;
import dealership.application.items.operations.testdrive.DeleteTestDrive;
import dealership.application.items.operations.testdrive.RemoveCarModelFromTestDriveList;
import dealership.application.items.operations.testdrive.UpdateTestDrive;

public interface TestDriveService {

    AddCarModelToTestDriveList.Response addCarModelToTestDriveList(AddCarModelToTestDriveList.Request request);

    RemoveCarModelFromTestDriveList.Response removeCarModelFromTestDriveList(RemoveCarModelFromTestDriveList.Request request);

    CreateTestDrive.Response createTestDrive(CreateTestDrive.Request request);

    CheckTestDrive.Response checkTestDrive(CheckTestDrive.Request request);

    CheckTestDriveList.Response checkTestDriveList(CheckTestDriveList.Request request);

    UpdateTestDrive.Response updateTestDrive(UpdateTestDrive.Request request);

    DeleteTestDrive.Response deleteTestDrive(DeleteTestDrive.Request request);
}

