package dealership.application.items.helpers;

import dealership.application.items.operations.testdrive.UpdateTestDrive;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.testdriverequest.TestDriveRequest;

public class TestDriveChangeApplier {
    public TestDriveRequest applyChange(TestDriveRequest testDriveRequest, UpdateTestDrive.Change change) {
        if (change instanceof UpdateTestDrive.ChangeStartAt changeStartAt) {
            if (changeStartAt.startAt() == null) {
                throw new DomainValidationException("Test drive start time must not be null.");
            }

            return new TestDriveRequest(
                    testDriveRequest.getId(),
                    testDriveRequest.getClient(),
                    testDriveRequest.getCarModel(),
                    changeStartAt.startAt()
            );
        }

        if (change instanceof UpdateTestDrive.ChangeCarModel) {
            throw new DomainValidationException(
                    "ChangeCarModel must be handled in TestDriveService before applying test drive changes."
            );
        }

        throw new DomainValidationException(
                "Unsupported test drive change type: " + change.getClass().getSimpleName()
        );
    }
}
