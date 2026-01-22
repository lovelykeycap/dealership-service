package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.CarConfigurationSelectedSlotJpa;
import dealership.infrastructure.persistence.entity.ids.CarConfigurationSelectedSlotJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarConfigurationSelectedSlotSpringDataRepository extends JpaRepository<CarConfigurationSelectedSlotJpa, CarConfigurationSelectedSlotJpaId> {
    void deleteAllByConfigurationId(UUID configurationId);

    List<CarConfigurationSelectedSlotJpa> findAllByConfigurationId(UUID configurationId);
}
