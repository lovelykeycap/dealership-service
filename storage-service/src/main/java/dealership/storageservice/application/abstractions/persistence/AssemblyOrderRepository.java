package dealership.storageservice.application.abstractions.persistence;

import dealership.storageservice.domain.assemblyorders.AssemblyOrder;

import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderRepository extends CrudRepository<AssemblyOrder> {
    Optional<AssemblyOrder> findBySourceOrderId(UUID sourceOrderId);
}
