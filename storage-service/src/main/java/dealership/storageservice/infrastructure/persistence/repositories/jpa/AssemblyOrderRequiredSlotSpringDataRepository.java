package dealership.storageservice.infrastructure.persistence.repositories.jpa;

import dealership.storageservice.infrastructure.persistence.entity.ids.AssemblyOrderRequiredSlotJpaId;
import dealership.storageservice.infrastructure.persistence.entity.jpa.AssemblyOrderRequiredSlotJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssemblyOrderRequiredSlotSpringDataRepository
        extends JpaRepository<AssemblyOrderRequiredSlotJpa, AssemblyOrderRequiredSlotJpaId> {
    List<AssemblyOrderRequiredSlotJpa> findAllByAssemblyOrderId(UUID assemblyOrderId);

    void deleteAllByAssemblyOrderId(UUID assemblyOrderId);
}
