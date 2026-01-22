package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.TestDriveAllowedModelJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestDriveAllowedModelSpringDataRepository extends JpaRepository<TestDriveAllowedModelJpa, UUID> {
}

