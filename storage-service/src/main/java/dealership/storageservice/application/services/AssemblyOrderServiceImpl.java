package dealership.storageservice.application.services;

import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.messaging.contracts.SourceOrderType;
import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.AssemblyOrderService;
import dealership.storageservice.application.items.operations.assemblyorder.AddAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrderList;
import dealership.storageservice.application.items.operations.assemblyorder.DeleteAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.UpdateAssemblyOrder;
import dealership.storageservice.domain.assemblyorders.AssemblyOrder;
import dealership.storageservice.domain.assemblyorders.AssemblyOrderSourceType;
import dealership.storageservice.domain.assemblyorders.AssemblyOrderStatus;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import dealership.storageservice.domain.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AssemblyOrderServiceImpl implements AssemblyOrderService {

    private final PersistenceContext persistenceContext;

    @Override
    @Transactional
    public AddAssemblyOrder.Response addAssemblyOrder(AddAssemblyOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Add assembly order request must not be null.");
        }
        if (request.sourceOrderId() == null) {
            throw new DomainValidationException("Assembly order source order id must not be null.");
        }

        if (persistenceContext.getAssemblyOrderRepository().findBySourceOrderId(request.sourceOrderId()).isPresent()) {
            throw new DomainValidationException(
                    "Assembly order for source order \"%s\" already exists.".formatted(request.sourceOrderId())
            );
        }

        AssemblyOrder assemblyOrder = new AssemblyOrder(
                UUID.randomUUID(),
                request.sourceOrderId(),
                parseSourceOrderType(request.sourceOrderType()),
                request.carId(),
                request.carModelId(),
                toRequiredSlotSet(request.requiredSlotVariantIds()),
                request.warehouseManagerId(),
                AssemblyOrderStatus.CREATED
        );

        persistenceContext.getAssemblyOrderRepository().save(assemblyOrder);
        return new AddAssemblyOrder.Response(assemblyOrder.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CheckAssemblyOrder.Response checkAssemblyOrder(CheckAssemblyOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check assembly order request must not be null.");
        }
        if (request.assemblyOrderId() == null) {
            throw new DomainValidationException("Assembly order id must not be null.");
        }

        AssemblyOrder assemblyOrder = findById(request.assemblyOrderId());
        return toCheckResponse(assemblyOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckAssemblyOrderList.Response checkAssemblyOrderList(CheckAssemblyOrderList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check assembly order list request must not be null.");
        }

        List<CheckAssemblyOrderList.AssemblyOrderDto> orders = persistenceContext.getAssemblyOrderRepository()
                .findAll()
                .stream()
                .map(this::toListItem)
                .toList();

        return new CheckAssemblyOrderList.Response(orders);
    }

    @Override
    @Transactional
    public UpdateAssemblyOrder.Response updateAssemblyOrder(UpdateAssemblyOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update assembly order request must not be null.");
        }
        if (request.assemblyOrderId() == null) {
            throw new DomainValidationException("Assembly order id must not be null.");
        }
        if (request.change() == null) {
            throw new DomainValidationException("Assembly order change must not be null.");
        }

        AssemblyOrder assemblyOrder = findById(request.assemblyOrderId());
        applyChange(assemblyOrder, request.change());
        persistenceContext.getAssemblyOrderRepository().save(assemblyOrder);

        return new UpdateAssemblyOrder.Response(assemblyOrder.getId());
    }

    @Override
    @Transactional
    public DeleteAssemblyOrder.Response deleteAssemblyOrder(DeleteAssemblyOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete assembly order request must not be null.");
        }
        if (request.assemblyOrderId() == null) {
            throw new DomainValidationException("Assembly order id must not be null.");
        }

        AssemblyOrder assemblyOrder = findById(request.assemblyOrderId());
        persistenceContext.getAssemblyOrderRepository().deleteById(assemblyOrder.getId());
        return new DeleteAssemblyOrder.Response(assemblyOrder.getId());
    }

    @Override
    @Transactional
    public void createOrUpdateFromApprovalEvent(OrderSentForApprovalEvent event) {
        if (event == null) {
            throw new DomainValidationException("Order sent for approval event must not be null.");
        }
        if (event.orderId() == null) {
            throw new DomainValidationException("Order sent for approval event order id must not be null.");
        }
        if (event.sourceOrderType() == null) {
            throw new DomainValidationException("Order sent for approval event source type must not be null.");
        }

        if (persistenceContext.getAssemblyOrderRepository().findBySourceOrderId(event.orderId()).isPresent()) {
            return;
        }

        AssemblyOrder created = new AssemblyOrder(
                UUID.randomUUID(),
                event.orderId(),
                fromMessagingSourceType(event.sourceOrderType()),
                event.carId(),
                event.carModelId(),
                event.slotVariantIds(),
                null,
                AssemblyOrderStatus.CREATED
        );
        persistenceContext.getAssemblyOrderRepository().save(created);
    }

    @Override
    @Transactional
    public void markAssembledBySourceOrderId(UUID sourceOrderId) {
        AssemblyOrder assemblyOrder = findBySourceOrderId(sourceOrderId);
        assemblyOrder.markAssembled();
        persistenceContext.getAssemblyOrderRepository().save(assemblyOrder);
    }

    @Override
    @Transactional
    public void markFailedBySourceOrderId(UUID sourceOrderId) {
        AssemblyOrder assemblyOrder = findBySourceOrderId(sourceOrderId);
        assemblyOrder.markFailed();
        persistenceContext.getAssemblyOrderRepository().save(assemblyOrder);
    }

    private void applyChange(AssemblyOrder assemblyOrder, UpdateAssemblyOrder.Change change) {
        if (change instanceof UpdateAssemblyOrder.ChangeStatus changeStatus) {
            assemblyOrder.changeStatus(parseStatus(changeStatus.status()));
            return;
        }
        if (change instanceof UpdateAssemblyOrder.AssignWarehouseManager assignWarehouseManager) {
            assemblyOrder.assignWarehouseManager(assignWarehouseManager.warehouseManagerId());
            return;
        }

        throw new DomainValidationException("Unsupported assembly order change type.");
    }

    private AssemblyOrder findById(UUID assemblyOrderId) {
        return persistenceContext.getAssemblyOrderRepository()
                .findById(assemblyOrderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Assembly order \"%s\" not found.".formatted(assemblyOrderId)
                ));
    }

    private AssemblyOrder findBySourceOrderId(UUID sourceOrderId) {
        if (sourceOrderId == null) {
            throw new DomainValidationException("Source order id must not be null.");
        }

        return persistenceContext.getAssemblyOrderRepository()
                .findBySourceOrderId(sourceOrderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Assembly order for source order \"%s\" not found.".formatted(sourceOrderId)
                ));
    }

    private CheckAssemblyOrder.Response toCheckResponse(AssemblyOrder assemblyOrder) {
        return new CheckAssemblyOrder.Response(
                assemblyOrder.getId(),
                assemblyOrder.getSourceOrderId(),
                assemblyOrder.getSourceOrderType().name(),
                assemblyOrder.getCarId(),
                assemblyOrder.getCarModelId(),
                List.copyOf(assemblyOrder.getRequiredSlotVariantIds()),
                assemblyOrder.getWarehouseManagerId(),
                assemblyOrder.getStatus().name()
        );
    }

    private CheckAssemblyOrderList.AssemblyOrderDto toListItem(AssemblyOrder assemblyOrder) {
        return new CheckAssemblyOrderList.AssemblyOrderDto(
                assemblyOrder.getId(),
                assemblyOrder.getSourceOrderId(),
                assemblyOrder.getSourceOrderType().name(),
                assemblyOrder.getCarId(),
                assemblyOrder.getCarModelId(),
                List.copyOf(assemblyOrder.getRequiredSlotVariantIds()),
                assemblyOrder.getWarehouseManagerId(),
                assemblyOrder.getStatus().name()
        );
    }

    private AssemblyOrderSourceType parseSourceOrderType(String rawSourceOrderType) {
        if (rawSourceOrderType == null || rawSourceOrderType.isBlank()) {
            throw new DomainValidationException("Assembly order source order type must not be blank.");
        }
        try {
            return AssemblyOrderSourceType.valueOf(
                    rawSourceOrderType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException(
                    "Unsupported assembly order source order type: \"%s\"."
                            .formatted(rawSourceOrderType)
            );
        }
    }

    private AssemblyOrderStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new DomainValidationException("Assembly order status must not be blank.");
        }
        try {
            return AssemblyOrderStatus.valueOf(
                    rawStatus.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException(
                    "Unsupported assembly order status: \"%s\"."
                            .formatted(rawStatus)
            );
        }
    }

    private AssemblyOrderSourceType fromMessagingSourceType(SourceOrderType sourceOrderType) {
        return switch (sourceOrderType) {
            case IN_STOCK -> AssemblyOrderSourceType.IN_STOCK;
            case CONFIGURED -> AssemblyOrderSourceType.CONFIGURED;
        };
    }

    private Set<UUID> toRequiredSlotSet(List<UUID> requiredSlotVariantIds) {
        LinkedHashSet<UUID> result = new LinkedHashSet<>();
        if (requiredSlotVariantIds == null) {
            return result;
        }

        for (UUID slotVariantId : requiredSlotVariantIds) {
            if (slotVariantId == null) {
                throw new DomainValidationException("Assembly order slot variant id must not be null.");
            }
            result.add(slotVariantId);
        }
        return result;
    }
}
