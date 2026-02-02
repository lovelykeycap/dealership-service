package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.ids.JwtSubClientId;
import dealership.infrastructure.persistence.entity.jpa.JwtSubClientJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JwtSubClientSpringDataRepository extends JpaRepository<JwtSubClientJpa, JwtSubClientId> {

    Optional<JwtSubClientJpa> findByIssuerAndSubject(String issuer, String subject);

    Optional<JwtSubClientJpa> findByClientId(UUID clientId);
}
