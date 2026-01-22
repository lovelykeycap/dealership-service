package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.TestDriveCarModelRepository;
import dealership.infrastructure.persistence.entity.mappers.TestDriveAllowedModelJpaMapper;
import dealership.infrastructure.persistence.repositories.jpa.TestDriveAllowedModelSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TestDriveCarModelRepositoryJpaAdapter implements TestDriveCarModelRepository {

    private final TestDriveAllowedModelSpringDataRepository testDriveAllowedModelSpringDataRepository;
    private final TestDriveAllowedModelJpaMapper testDriveAllowedModelJpaMapper;

    @Override
    @Transactional
    public void addModelId(UUID modelId) {
        requireModelId(modelId);
        testDriveAllowedModelSpringDataRepository.save(testDriveAllowedModelJpaMapper.toJpa(modelId));
    }

    @Override
    @Transactional
    public void removeModelId(UUID modelId) {
        requireModelId(modelId);
        testDriveAllowedModelSpringDataRepository.deleteById(modelId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean containsModelId(UUID modelId) {
        requireModelId(modelId);
        return testDriveAllowedModelSpringDataRepository.existsById(modelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> findAllModelIds() {
        return testDriveAllowedModelSpringDataRepository.findAll().stream()
                .map(testDriveAllowedModelJpaMapper::toDomain)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void requireModelId(UUID modelId) {
        if (modelId == null) {
            throw new IllegalStateException("Car model id must not be null.");
        }
    }
}
