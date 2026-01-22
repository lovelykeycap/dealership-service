package dealership.infrastructure.persistence.repositories.jpa;

import dealership.infrastructure.persistence.entity.jpa.OrderJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderSpringDataRepository extends JpaRepository<OrderJpa, UUID> {

    @Query("select o.status from OrderJpa o where o.id = :orderId")
    String findStatusById(@Param("orderId") UUID orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderJpa o
            set o.status = :newStatus
            where o.id = :orderId
              and o.status = :expectedCurrentStatus
            """)
    int updateStatusIfCurrent(
            @Param("orderId") UUID orderId,
            @Param("expectedCurrentStatus") String expectedCurrentStatus,
            @Param("newStatus") String newStatus
    );
}
