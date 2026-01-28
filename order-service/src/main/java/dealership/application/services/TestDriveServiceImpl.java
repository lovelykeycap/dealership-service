package dealership.application.services;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.persistence.PersistenceContext;
import dealership.application.contracts.TestDriveService;
import dealership.application.items.helpers.TestDriveChangeApplier;
import dealership.application.items.operations.testdrive.AddCarModelToTestDriveList;
import dealership.application.items.operations.testdrive.CheckTestDrive;
import dealership.application.items.operations.testdrive.CheckTestDriveList;
import dealership.application.items.operations.testdrive.CreateTestDrive;
import dealership.application.items.operations.testdrive.DeleteTestDrive;
import dealership.application.items.operations.testdrive.RemoveCarModelFromTestDriveList;
import dealership.application.items.operations.testdrive.UpdateTestDrive;
import dealership.application.mappers.TestDriveDtoMapper;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.persons.Client;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.EntityNotFoundException;
import dealership.domain.testdriverequest.TestDriveRequest;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TestDriveServiceImpl implements TestDriveService {
    private final PersistenceContext persistenceContext;
    private final TestDriveDtoMapper testDriveDtoMapper;
    private final TestDriveChangeApplier testDriveChangeApplier;
    private final StorageCatalogGateway storageCatalogGateway;

    @Override
    public AddCarModelToTestDriveList.Response addCarModelToTestDriveList(AddCarModelToTestDriveList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Add car model to test drive list request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }

        CarModel carModel = findCarModelById(request.modelId());
        persistenceContext.getTestDriveCarModelRepository().addModelId(carModel.getId());
        return new AddCarModelToTestDriveList.Response(carModel.getId());
    }

    @Override
    public RemoveCarModelFromTestDriveList.Response removeCarModelFromTestDriveList(
            RemoveCarModelFromTestDriveList.Request request
    ) {
        if (request == null) {
            throw new DomainValidationException("Remove car model from test drive list request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (!persistenceContext.getTestDriveCarModelRepository().containsModelId(request.modelId())) {
            throw new DomainValidationException(
                    "Car model \"%s\" is not available for test drive.".formatted(request.modelId())
            );
        }

        persistenceContext.getTestDriveCarModelRepository().removeModelId(request.modelId());
        return new RemoveCarModelFromTestDriveList.Response(request.modelId());
    }

    @Override
    public CreateTestDrive.Response createTestDrive(CreateTestDrive.Request request) {
        if (request == null) {
            throw new DomainValidationException("Create test drive request must not be null.");
        }
        if (request.clientId() == null) {
            throw new DomainValidationException("Client id must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (request.startAt() == null) {
            throw new DomainValidationException("Test drive start time must not be null.");
        }

        Client client = findClientById(request.clientId());
        CarModel carModel = findCarModelAvailableForTestDrive(request.modelId());

        UUID id = UUID.randomUUID();
        TestDriveRequest testDriveRequest = new TestDriveRequest(
                id,
                client,
                carModel,
                request.startAt()
        );

        persistenceContext.getTestDriveRequestRepository().save(testDriveRequest);
        return new CreateTestDrive.Response(id);
    }

    @Override
    public CheckTestDrive.Response checkTestDrive(CheckTestDrive.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check test drive request must not be null.");
        }
        if (request.id() == null) {
            throw new DomainValidationException("Test drive request id must not be null.");
        }

        TestDriveRequest testDriveRequest = findTestDriveById(request.id());
        return testDriveDtoMapper.toCheckTestDriveResponse(testDriveRequest);
    }

    @Override
    public CheckTestDriveList.Response checkTestDriveList(CheckTestDriveList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check test drive list request must not be null.");
        }

        List<CheckTestDriveList.TestDriveRequestDto> requests = persistenceContext.getTestDriveRequestRepository()
                .findAll()
                .stream()
                .map(testDriveDtoMapper::toTestDriveRequestDto)
                .toList();

        return new CheckTestDriveList.Response(requests);
    }

    @Override
    public UpdateTestDrive.Response updateTestDrive(UpdateTestDrive.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update test drive request must not be null.");
        }
        if (request.id() == null) {
            throw new DomainValidationException("Test drive request id must not be null.");
        }
        if (request.changes() == null) {
            throw new DomainValidationException("Test drive changes must not be null.");
        }

        TestDriveRequest updatedTestDriveRequest = findTestDriveById(request.id());

        for (UpdateTestDrive.Change change : request.changes()) {
            if (change == null) {
                throw new DomainValidationException("Test drive change must not be null.");
            }

            if (change instanceof UpdateTestDrive.ChangeCarModel changeCarModel) {
                if (changeCarModel.modelId() == null) {
                    throw new DomainValidationException("Car model id must not be null.");
                }

                updatedTestDriveRequest = new TestDriveRequest(
                        updatedTestDriveRequest.getId(),
                        updatedTestDriveRequest.getClient(),
                        findCarModelAvailableForTestDrive(changeCarModel.modelId()),
                        updatedTestDriveRequest.getStartAt()
                );
                continue;
            }

            updatedTestDriveRequest = testDriveChangeApplier.applyChange(updatedTestDriveRequest, change);
        }

        persistenceContext.getTestDriveRequestRepository().save(updatedTestDriveRequest);
        return new UpdateTestDrive.Response(updatedTestDriveRequest.getId());
    }

    @Override
    public DeleteTestDrive.Response deleteTestDrive(DeleteTestDrive.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete test drive request must not be null.");
        }
        if (request.id() == null) {
            throw new DomainValidationException("Test drive request id must not be null.");
        }

        TestDriveRequest testDriveRequest = findTestDriveById(request.id());
        persistenceContext.getTestDriveRequestRepository().deleteById(testDriveRequest.getId());
        return new DeleteTestDrive.Response(testDriveRequest.getId());
    }

    private Client findClientById(UUID clientId) {
        return persistenceContext.getClientRepository()
                .findById(clientId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Client \"%s\" not found".formatted(clientId)
                        )
                );
    }

    private TestDriveRequest findTestDriveById(UUID id) {
        return persistenceContext.getTestDriveRequestRepository()
                .findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Test drive request \"%s\" not found".formatted(id)
                        )
                );
    }

    private CarModel findCarModelAvailableForTestDrive(UUID modelId) {
        CarModel carModel = findCarModelById(modelId);

        if (!persistenceContext.getTestDriveCarModelRepository().containsModelId(carModel.getId())) {
            throw new DomainValidationException(
                    "Car model \"%s\" is not available for test drive.".formatted(carModel.getId())
            );
        }

        return carModel;
    }

    private CarModel findCarModelById(UUID modelId) {
        return storageCatalogGateway.getCarModel(modelId);
    }
}
