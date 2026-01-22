package dealership.storageservice.infrastructure.persistence.repositories.jpa;

import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelBaseSlotJpa;
import dealership.storageservice.infrastructure.persistence.entity.ids.CarModelBaseSlotJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarModelBaseSlotSpringDataRepository extends JpaRepository<CarModelBaseSlotJpa, CarModelBaseSlotJpaId> {

    void deleteAllByCarModelId(UUID carModelId);

    List<CarModelBaseSlotJpa> findAllByCarModelId(UUID carModelId);
}
