package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.CarJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarSpringDataRepository extends JpaRepository<CarJpa, UUID> {
}

