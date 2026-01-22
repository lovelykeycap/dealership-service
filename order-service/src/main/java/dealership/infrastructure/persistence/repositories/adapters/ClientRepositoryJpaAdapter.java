package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.ClientRepository;
import dealership.domain.entities.persons.Client;
import dealership.infrastructure.persistence.entity.mappers.ClientJpaMapper;
import dealership.infrastructure.persistence.repositories.jpa.ClientSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class ClientRepositoryJpaAdapter implements ClientRepository {

    private final ClientSpringDataRepository clientSpringDataRepository;
    private final ClientJpaMapper clientJpaMapper;

    @Override
    @Transactional
    public Client save(Client entity) {
        var savedJpa = clientSpringDataRepository.save(clientJpaMapper.toJpa(entity));
        return clientJpaMapper.toDomain(savedJpa);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(UUID id) {
        return clientSpringDataRepository.findById(id)
                .map(clientJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientSpringDataRepository.findAll().stream()
                .map(clientJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        clientSpringDataRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return clientSpringDataRepository.existsById(id);
    }
}
