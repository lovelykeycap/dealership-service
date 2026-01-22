package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.CarConfigurationJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarConfigurationSpringDataRepository extends JpaRepository<CarConfigurationJpa, UUID> {
}

