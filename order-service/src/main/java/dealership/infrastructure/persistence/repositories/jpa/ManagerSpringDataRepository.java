package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.ManagerJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagerSpringDataRepository extends JpaRepository<ManagerJpa, UUID> {
}

