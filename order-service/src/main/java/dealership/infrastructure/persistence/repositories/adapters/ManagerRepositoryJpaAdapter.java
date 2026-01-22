package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.ManagerRepository;
import dealership.domain.entities.persons.Manager;
import dealership.infrastructure.persistence.entity.mappers.ManagerJpaMapper;
import dealership.infrastructure.persistence.repositories.jpa.ManagerSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class ManagerRepositoryJpaAdapter implements ManagerRepository {

    private final ManagerSpringDataRepository managerSpringDataRepository;
    private final ManagerJpaMapper managerJpaMapper;

    @Override
    @Transactional
    public Manager save(Manager entity) {
        var savedJpa = managerSpringDataRepository.save(managerJpaMapper.toJpa(entity));
        return managerJpaMapper.toDomain(savedJpa);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Manager> findById(UUID id) {
        return managerSpringDataRepository.findById(id)
                .map(managerJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Manager> findAll() {
        return managerSpringDataRepository.findAll().stream()
                .map(managerJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        managerSpringDataRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return managerSpringDataRepository.existsById(id);
    }
}
