package dealership.storageservice.infrastructure.persistence.repositories.adapters;

import dealership.storageservice.application.abstractions.persistence.AssemblyOrderRepository;
import dealership.storageservice.domain.assemblyorders.AssemblyOrder;
import dealership.storageservice.domain.assemblyorders.AssemblyOrderSourceType;
import dealership.storageservice.domain.assemblyorders.AssemblyOrderStatus;
import dealership.storageservice.infrastructure.persistence.entity.jpa.AssemblyOrderJpa;
import dealership.storageservice.infrastructure.persistence.entity.jpa.AssemblyOrderRequiredSlotJpa;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.AssemblyOrderRequiredSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.AssemblyOrderSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AssemblyOrderRepositoryJpaAdapter implements AssemblyOrderRepository {

    private final AssemblyOrderSpringDataRepository assemblyOrderSpringDataRepository;
    private final AssemblyOrderRequiredSlotSpringDataRepository assemblyOrderRequiredSlotSpringDataRepository;

    @Override
    @Transactional
    public AssemblyOrder save(AssemblyOrder entity) {
        UUID assemblyOrderId = requireId(entity.getId(), "Assembly order id must not be null.");

        Optional<AssemblyOrderJpa> existing = assemblyOrderSpringDataRepository.findById(assemblyOrderId);
        String sourceOrderType = entity.getSourceOrderType().name();
        String status = entity.getStatus().name();

        if (existing.isPresent()) {
            AssemblyOrderJpa row = existing.get();
            row.setSourceOrderId(entity.getSourceOrderId());
            row.setSourceOrderType(sourceOrderType);
            row.setCarId(entity.getCarId());
            row.setCarModelId(entity.getCarModelId());
            row.setWarehouseManagerId(entity.getWarehouseManagerId());
            row.setStatus(status);
            assemblyOrderSpringDataRepository.save(row);
        } else {
            assemblyOrderSpringDataRepository.save(new AssemblyOrderJpa(
                    assemblyOrderId,
                    entity.getSourceOrderId(),
                    sourceOrderType,
                    entity.getCarId(),
                    entity.getCarModelId(),
                    entity.getWarehouseManagerId(),
                    status
            ));
        }

        assemblyOrderRequiredSlotSpringDataRepository.deleteAllByAssemblyOrderId(assemblyOrderId);

        List<AssemblyOrderRequiredSlotJpa> requiredSlots = entity.getRequiredSlotVariantIds()
                .stream()
                .map(slotVariantId -> new AssemblyOrderRequiredSlotJpa(assemblyOrderId, slotVariantId))
                .toList();
        assemblyOrderRequiredSlotSpringDataRepository.saveAll(requiredSlots);

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssemblyOrder> findById(UUID id) {
        return assemblyOrderSpringDataRepository.findById(id)
                .map(this::assembleDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssemblyOrder> findAll() {
        return assemblyOrderSpringDataRepository.findAll()
                .stream()
                .map(this::assembleDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        assemblyOrderSpringDataRepository.findById(id).ifPresent(assemblyOrderRow -> {
            assemblyOrderRequiredSlotSpringDataRepository.deleteAllByAssemblyOrderId(id);
            assemblyOrderSpringDataRepository.deleteById(id);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return assemblyOrderSpringDataRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssemblyOrder> findBySourceOrderId(UUID sourceOrderId) {
        return assemblyOrderSpringDataRepository.findBySourceOrderId(sourceOrderId)
                .map(this::assembleDomain);
    }

    private AssemblyOrder assembleDomain(AssemblyOrderJpa row) {
        Set<UUID> requiredSlotVariantIds = assemblyOrderRequiredSlotSpringDataRepository
                .findAllByAssemblyOrderId(row.getId())
                .stream()
                .map(AssemblyOrderRequiredSlotJpa::getSlotVariantId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return new AssemblyOrder(
                row.getId(),
                row.getSourceOrderId(),
                parseSourceType(row.getSourceOrderType()),
                row.getCarId(),
                row.getCarModelId(),
                requiredSlotVariantIds,
                row.getWarehouseManagerId(),
                parseStatus(row.getStatus())
        );
    }

    private AssemblyOrderSourceType parseSourceType(String rawSourceType) {
        if (rawSourceType == null || rawSourceType.isBlank()) {
            throw new IllegalStateException("Assembly order source type must not be blank in persistence data.");
        }
        try {
            return AssemblyOrderSourceType.valueOf(
                    rawSourceType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported assembly order source type in persistence data: " + rawSourceType, ex);
        }
    }

    private AssemblyOrderStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalStateException("Assembly order status must not be blank in persistence data.");
        }
        try {
            return AssemblyOrderStatus.valueOf(
                    rawStatus.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported assembly order status in persistence data: " + rawStatus, ex);
        }
    }

    private UUID requireId(UUID id, String message) {
        if (id == null) {
            throw new IllegalStateException(message);
        }
        return id;
    }
}
