package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.JwtSubRepository;
import dealership.infrastructure.persistence.entity.jpa.JwtSubClientJpa;
import dealership.infrastructure.persistence.repositories.jpa.JwtSubClientSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class JwtSubRepositoryJpaAdapter implements JwtSubRepository {

    private final JwtSubClientSpringDataRepository jwtSubClientSpringDataRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findClientIdByIssuerAndSubject(String issuer, String subject) {
        return jwtSubClientSpringDataRepository.findByIssuerAndSubject(issuer, subject)
                .map(JwtSubClientJpa::getClientId);
    }

    @Override
    @Transactional
    public void bindClient(String issuer, String subject, UUID clientId) {
        jwtSubClientSpringDataRepository.save(new JwtSubClientJpa(subject, issuer, clientId));
    }
}
