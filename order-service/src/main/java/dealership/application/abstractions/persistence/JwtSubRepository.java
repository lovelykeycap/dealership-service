package dealership.application.abstractions.persistence;

import java.util.Optional;
import java.util.UUID;

public interface JwtSubRepository {

    Optional<UUID> findClientIdByIssuerAndSubject(String issuer, String subject);

    void bindClient(String issuer, String subject, UUID clientId);
}
