package dealership.storageservice.infrastructure.persistence.repositories.jpa;

import dealership.storageservice.infrastructure.persistence.entity.jpa.AssemblyOrderJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderSpringDataRepository extends JpaRepository<AssemblyOrderJpa, UUID> {
    Optional<AssemblyOrderJpa> findBySourceOrderId(UUID sourceOrderId);
}
