package dealership.storageservice.infrastructure.persistence.repositories.jpa;

import dealership.storageservice.infrastructure.persistence.entity.jpa.SlotVariantJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlotVariantSpringDataRepository extends JpaRepository<SlotVariantJpa, UUID> {
}

