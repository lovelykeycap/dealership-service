package dealership.application.mappers;

import dealership.application.items.operations.testdrive.CheckTestDrive;
import dealership.application.items.operations.testdrive.CheckTestDriveList;
import dealership.domain.testdriverequest.TestDriveRequest;

public class TestDriveDtoMapper {
    public CheckTestDrive.Response toCheckTestDriveResponse(TestDriveRequest testDriveRequest) {
        return new CheckTestDrive.Response(
                testDriveRequest.getId(),
                testDriveRequest.getClient().getId(),
                testDriveRequest.getCarModel().getId(),
                testDriveRequest.getStartAt()
        );
    }

    public CheckTestDriveList.TestDriveRequestDto toTestDriveRequestDto(TestDriveRequest testDriveRequest) {
        return new CheckTestDriveList.TestDriveRequestDto(
                testDriveRequest.getId(),
                testDriveRequest.getClient().getId(),
                testDriveRequest.getCarModel().getId(),
                testDriveRequest.getStartAt()
        );
    }
}
