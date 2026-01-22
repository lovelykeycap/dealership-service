package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.TestDriveRequestJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestDriveRequestSpringDataRepository extends JpaRepository<TestDriveRequestJpa, UUID> {
}

