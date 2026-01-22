package dealership.storageservice.infrastructure.persistence.repositories.jpa;

import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CarModelSpringDataRepository
        extends JpaRepository<CarModelJpa, UUID>, JpaSpecificationExecutor<CarModelJpa> {
}
