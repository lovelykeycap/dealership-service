package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.SlotVariantCompatibleCarModelJpa;
import dealership.infrastructure.persistence.entity.ids.SlotVariantCompatibleCarModelJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SlotVariantCompatibleCarModelSpringDataRepository extends JpaRepository<SlotVariantCompatibleCarModelJpa, SlotVariantCompatibleCarModelJpaId> {
    void deleteAllBySlotVariantId(UUID slotVariantId);

    List<SlotVariantCompatibleCarModelJpa> findAllBySlotVariantId(UUID slotVariantId);
}
