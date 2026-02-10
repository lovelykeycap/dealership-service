package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.OutboxEventJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventSpringDataRepository extends JpaRepository<OutboxEventJpa, UUID> {

    @Query("""
            SELECT e
            FROM OutboxEventJpa e
            WHERE (e.status = 'NEW' OR e.status = 'FAILED')
              AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEventJpa> findReadyForPublish(@Param("now") Instant now, Pageable pageable);
}
