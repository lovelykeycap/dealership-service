package dealership.storageservice.domain.assemblyorders;

import dealership.storageservice.domain.exceptions.DomainValidationException;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class AssemblyOrder {
    private final UUID id;
    private final UUID sourceOrderId;
    private final AssemblyOrderSourceType sourceOrderType;
    private final UUID carId;
    private final UUID carModelId;
    private final Set<UUID> requiredSlotVariantIds;

    private UUID warehouseManagerId;
    private AssemblyOrderStatus status;

    public AssemblyOrder(
            UUID id,
            UUID sourceOrderId,
            AssemblyOrderSourceType sourceOrderType,
            UUID carId,
            UUID carModelId,
            Set<UUID> requiredSlotVariantIds,
            UUID warehouseManagerId,
            AssemblyOrderStatus status
    ) {
        if (id == null) {
            throw new DomainValidationException("Assembly order id must not be null.");
        }
        if (sourceOrderId == null) {
            throw new DomainValidationException("Assembly order source order id must not be null.");
        }
        if (sourceOrderType == null) {
            throw new DomainValidationException("Assembly order source order type must not be null.");
        }
        if (status == null) {
            throw new DomainValidationException("Assembly order status must not be null.");
        }

        LinkedHashSet<UUID> normalizedSlotIds = new LinkedHashSet<>();
        if (requiredSlotVariantIds != null) {
            for (UUID slotVariantId : requiredSlotVariantIds) {
                if (slotVariantId == null) {
                    throw new DomainValidationException("Assembly order slot variant id must not be null.");
                }
                normalizedSlotIds.add(slotVariantId);
            }
        }

        if (sourceOrderType == AssemblyOrderSourceType.IN_STOCK && carId == null) {
            throw new DomainValidationException("In-stock assembly order must reference a car id.");
        }
        if (sourceOrderType == AssemblyOrderSourceType.CONFIGURED) {
            if (carModelId == null) {
                throw new DomainValidationException("Configured assembly order must reference a car model id.");
            }
            if (normalizedSlotIds.isEmpty()) {
                throw new DomainValidationException(
                        "Configured assembly order must contain required slot variant ids."
                );
            }
        }

        this.id = id;
        this.sourceOrderId = sourceOrderId;
        this.sourceOrderType = sourceOrderType;
        this.carId = carId;
        this.carModelId = carModelId;
        this.requiredSlotVariantIds = normalizedSlotIds;
        this.warehouseManagerId = warehouseManagerId;
        this.status = status;
    }

    public Set<UUID> getRequiredSlotVariantIds() {
        return Collections.unmodifiableSet(requiredSlotVariantIds);
    }

    public void assignWarehouseManager(UUID warehouseManagerId) {
        if (warehouseManagerId == null) {
            throw new DomainValidationException("Warehouse manager id must not be null.");
        }
        this.warehouseManagerId = warehouseManagerId;
    }

    public void changeStatus(AssemblyOrderStatus newStatus) {
        if (newStatus == null) {
            throw new DomainValidationException("Assembly order status must not be null.");
        }
        this.status = newStatus;
    }

    public void markAssembled() {
        this.status = AssemblyOrderStatus.ASSEMBLED;
    }

    public void markFailed() {
        this.status = AssemblyOrderStatus.FAIL;
    }
}
