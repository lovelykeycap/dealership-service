package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.ClientJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientSpringDataRepository extends JpaRepository<ClientJpa, UUID> {
}
