package dealership.storageservice.infrastructure.messaging;

import dealership.messaging.contracts.OrderApprovedEvent;
import dealership.messaging.contracts.OrderRejectedEvent;
import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.messaging.contracts.RabbitContracts;
import dealership.messaging.contracts.SourceOrderType;
import dealership.storageservice.application.contracts.AssemblyOrderService;
import dealership.storageservice.domain.assemblyorders.AssemblyOrder;
import dealership.storageservice.domain.assemblyorders.AssemblyOrderStatus;
import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.domain.entities.cars.Car;
import dealership.storageservice.domain.entities.cars.CarStockStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSentForApprovalListener {

    private final PersistenceContext persistenceContext;
    private final AssemblyOrderService assemblyOrderService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitContracts.STORAGE_APPROVAL_QUEUE)
    public void onOrderSentForApproval(OrderSentForApprovalEvent event) {
        if (event == null || event.orderId() == null) {
            log.warn("Received invalid OrderSentForApprovalEvent payload: {}", event);
            return;
        }

        String traceId = safeTraceId(event.traceId());
        if (replayIfAlreadyTerminal(event.orderId(), traceId)) {
            return;
        }

        try {
            assemblyOrderService.createOrUpdateFromApprovalEvent(event);
            validateOrderForApproval(event);
            assemblyOrderService.markAssembledBySourceOrderId(event.orderId());
            UUID assemblyOrderId = persistenceContext.getAssemblyOrderRepository()
                    .findBySourceOrderId(event.orderId())
                    .map(order -> order.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Assembly order was not found after marking as assembled. sourceOrderId=\"%s\""
                                    .formatted(event.orderId())
                    ));
            publishApproved(event.orderId(), assemblyOrderId, traceId);
        } catch (Exception ex) {
            try {
                assemblyOrderService.markFailedBySourceOrderId(event.orderId());
            } catch (Exception markFailEx) {
                log.warn(
                        "Failed to mark assembly order as FAIL for sourceOrderId={}, reason={}",
                        event.orderId(),
                        markFailEx.getMessage()
                );
            }
            publishRejected(event.orderId(), traceId, ex.getMessage());
        }
    }

    private void validateOrderForApproval(OrderSentForApprovalEvent event) {
        SourceOrderType sourceOrderType = event.sourceOrderType();
        if (sourceOrderType == null) {
            throw new IllegalStateException("Source order type is missing.");
        }

        if (sourceOrderType == SourceOrderType.IN_STOCK) {
            validateInStock(event);
            return;
        }
        if (sourceOrderType == SourceOrderType.CONFIGURED) {
            validateConfigured(event);
            return;
        }

        throw new IllegalStateException(
                "Unsupported source order type: %s".formatted(sourceOrderType)
        );
    }

    private void validateInStock(OrderSentForApprovalEvent event) {
        UUID carId = event.carId();
        if (carId == null) {
            throw new IllegalStateException("In-stock order must contain carId.");
        }

        Car car = persistenceContext.getCarRepository()
                .findById(carId)
                .orElseThrow(() -> new IllegalStateException(
                        "Car \"%s\" not found in storage.".formatted(carId)
                ));

        if (car.getStatus() != CarStockStatus.RESERVED) {
            if (car.getStatus() == CarStockStatus.AVAILABLE) {
                car.changeStatus(CarStockStatus.RESERVED);
                persistenceContext.getCarRepository().save(car);
                return;
            }

            throw new IllegalStateException(
                    "Car \"%s\" has invalid stock status \"%s\" for approval."
                            .formatted(carId, car.getStatus())
            );
        }
    }

    private void validateConfigured(OrderSentForApprovalEvent event) {
        UUID modelId = event.carModelId();
        if (modelId == null) {
            throw new IllegalStateException("Configured order must contain carModelId.");
        }

        persistenceContext.getCarModelRepository()
                .findById(modelId)
                .orElseThrow(() -> new IllegalStateException(
                        "Car model \"%s\" not found in storage.".formatted(modelId)
                ));

        Set<UUID> slotVariantIds = event.slotVariantIds();
        if (slotVariantIds == null || slotVariantIds.isEmpty()) {
            throw new IllegalStateException("Configured order must contain slotVariantIds.");
        }

        for (UUID slotVariantId : slotVariantIds) {
            if (slotVariantId == null) {
                throw new IllegalStateException("Configured order contains null slotVariantId.");
            }

            persistenceContext.getSlotVariantRepository()
                    .findById(slotVariantId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Slot variant \"%s\" not found in storage.".formatted(slotVariantId)
                    ));

            if (!persistenceContext.getSlotVariantStockRepository().hasAtLeast(slotVariantId, 1)) {
                throw new IllegalStateException(
                        "Not enough stock for slot variant \"%s\".".formatted(slotVariantId)
                );
            }
        }

        for (UUID slotVariantId : slotVariantIds) {
            persistenceContext.getSlotVariantStockRepository().removeQuantity(slotVariantId, 1);
        }
    }

    private void publishApproved(UUID orderId, UUID assemblyOrderId, String traceId) {
        OrderApprovedEvent approvedEvent = new OrderApprovedEvent(
                orderId,
                traceId,
                assemblyOrderId,
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                RabbitContracts.ORDERS_EXCHANGE,
                RabbitContracts.ORDER_APPROVED_ROUTING_KEY,
                approvedEvent
        );

        log.info("Order approved in storage. orderId={}, traceId={}", orderId, traceId);
    }

    private void publishRejected(UUID orderId, String traceId, String reason) {
        String rejectionReason = (reason == null || reason.isBlank())
                ? "Storage approval failed."
                : reason;

        OrderRejectedEvent rejectedEvent = new OrderRejectedEvent(
                orderId,
                traceId,
                rejectionReason,
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                RabbitContracts.ORDERS_EXCHANGE,
                RabbitContracts.ORDER_REJECTED_ROUTING_KEY,
                rejectedEvent
        );

        log.warn(
                "Order rejected in storage. orderId={}, traceId={}, reason={}",
                orderId,
                traceId,
                rejectionReason
        );
    }

    private String safeTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }

    private boolean replayIfAlreadyTerminal(UUID sourceOrderId, String traceId) {
        AssemblyOrder existing = persistenceContext.getAssemblyOrderRepository()
                .findBySourceOrderId(sourceOrderId)
                .orElse(null);
        if (existing == null) {
            return false;
        }

        if (existing.getStatus() == AssemblyOrderStatus.ASSEMBLED) {
            publishApproved(sourceOrderId, existing.getId(), traceId);
            log.info(
                    "Replayed approval result for duplicate event. sourceOrderId={}, assemblyOrderId={}, traceId={}",
                    sourceOrderId,
                    existing.getId(),
                    traceId
            );
            return true;
        }

        if (existing.getStatus() == AssemblyOrderStatus.FAIL) {
            publishRejected(sourceOrderId, traceId, "Assembly order is already in FAIL status.");
            log.info(
                    "Replayed rejection result for duplicate event. sourceOrderId={}, assemblyOrderId={}, traceId={}",
                    sourceOrderId,
                    existing.getId(),
                    traceId
            );
            return true;
        }

        return false;
    }
}
