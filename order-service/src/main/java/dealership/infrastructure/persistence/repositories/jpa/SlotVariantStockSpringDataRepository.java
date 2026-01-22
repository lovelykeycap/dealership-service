package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.SlotVariantStockJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlotVariantStockSpringDataRepository extends JpaRepository<SlotVariantStockJpa, UUID> {
    Optional<SlotVariantStockJpa> findBySlotVariantId(UUID slotVariantId);
}

