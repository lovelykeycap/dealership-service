package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.persistence.ClientRepository;
import dealership.application.abstractions.persistence.TestDriveRequestRepository;
import dealership.domain.testdriverequest.TestDriveRequest;
import dealership.infrastructure.persistence.entity.jpa.TestDriveRequestJpa;
import dealership.infrastructure.persistence.repositories.jpa.TestDriveRequestSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class TestDriveRequestRepositoryJpaAdapter implements TestDriveRequestRepository {

    private final TestDriveRequestSpringDataRepository testDriveRequestSpringDataRepository;
    private final ClientRepository clientRepository;
    private final StorageCatalogGateway storageCatalogGateway;

    @Override
    @Transactional
    public TestDriveRequest save(TestDriveRequest entity) {
        UUID requestId = requireId(entity.getId(), "Test drive request id must not be null.");
        UUID clientId = requireId(entity.getClient().getId(), "Test drive request client id must not be null.");
        UUID carModelId = requireId(entity.getCarModel().getId(), "Test drive request car model id must not be null.");
        if (entity.getStartAt() == null) {
            throw new IllegalStateException("Test drive request start time must not be null.");
        }

        testDriveRequestSpringDataRepository.save(
                new TestDriveRequestJpa(requestId, clientId, carModelId, entity.getStartAt())
        );
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestDriveRequest> findById(UUID id) {
        return testDriveRequestSpringDataRepository.findById(id)
                .map(this::assembleDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveRequest> findAll() {
        return testDriveRequestSpringDataRepository.findAll().stream()
                .map(this::assembleDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        testDriveRequestSpringDataRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return testDriveRequestSpringDataRepository.existsById(id);
    }

    private TestDriveRequest assembleDomain(TestDriveRequestJpa jpa) {
        var client = clientRepository.findById(jpa.getClientId())
                .orElseThrow(() -> new IllegalStateException(
                        "Client \"%s\" referenced by test drive request \"%s\" was not found."
                                .formatted(jpa.getClientId(), jpa.getId())
                ));

        var carModel = storageCatalogGateway.getCarModel(jpa.getCarModelId());

        return new TestDriveRequest(
                jpa.getId(),
                client,
                carModel,
                jpa.getStartAt()
        );
    }

    private UUID requireId(UUID id, String message) {
        if (id == null) {
            throw new IllegalStateException(message);
        }
        return id;
    }
}
