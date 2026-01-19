package dealership.storageservice.application.abstractions.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrudRepository<T> {
    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}

