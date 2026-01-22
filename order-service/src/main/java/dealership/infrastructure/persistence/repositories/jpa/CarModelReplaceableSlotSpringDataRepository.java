package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.CarModelReplaceableSlotJpa;
import dealership.infrastructure.persistence.entity.ids.CarModelReplaceableSlotJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarModelReplaceableSlotSpringDataRepository extends JpaRepository<CarModelReplaceableSlotJpa, CarModelReplaceableSlotJpaId> {
    void deleteAllByCarModelId(UUID carModelId);

    List<CarModelReplaceableSlotJpa> findAllByCarModelId(UUID carModelId);
}
