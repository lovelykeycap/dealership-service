package dealership.infrastructure.messaging;

import dealership.messaging.contracts.OrderApprovedEvent;
import dealership.messaging.contracts.OrderRejectedEvent;
import dealership.messaging.contracts.RabbitContracts;
import dealership.infrastructure.persistence.repositories.jpa.OrderSpringDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = RabbitContracts.ORDER_APPROVAL_RESULT_QUEUE)
public class OrderApprovalResultListener {

    private static final String PAID_STATUS = "Paid";
    private static final String READY_FOR_PICKUP_STATUS = "Ready for pickup";
    private static final String CANCELLED_STATUS = "Cancelled";

    private final OrderSpringDataRepository orderSpringDataRepository;

    @RabbitHandler
    @Transactional
    public void onApproved(OrderApprovedEvent event) {
        String currentStatus = orderSpringDataRepository.findStatusById(event.orderId());
        if (currentStatus == null) {
            log.warn(
                    "OrderApproved received for unknown order. orderId={}, traceId={}",
                    event.orderId(),
                    event.traceId()
            );
            return;
        }

        if (!PAID_STATUS.equals(currentStatus)) {
            log.info(
                    "OrderApproved skipped: order already processed. orderId={}, status={}, traceId={}",
                    event.orderId(),
                    currentStatus,
                    event.traceId()
            );
            return;
        }

        int updatedRows = orderSpringDataRepository.updateStatusIfCurrent(
                event.orderId(),
                PAID_STATUS,
                READY_FOR_PICKUP_STATUS
        );

        if (updatedRows == 0) {
            String actualStatus = orderSpringDataRepository.findStatusById(event.orderId());
            log.info(
                    "OrderApproved skipped due concurrent status change. orderId={}, expectedStatus={}, actualStatus={}, traceId={}",
                    event.orderId(),
                    PAID_STATUS,
                    actualStatus,
                    event.traceId()
            );
            return;
        }

        log.info(
                    "Order approved by storage. orderId={}, status={}, traceId={}",
                    event.orderId(),
                    READY_FOR_PICKUP_STATUS,
                    event.traceId()
        );
    }

    @RabbitHandler
    @Transactional
    public void onRejected(OrderRejectedEvent event) {
        String currentStatus = orderSpringDataRepository.findStatusById(event.orderId());
        if (currentStatus == null) {
            log.warn(
                    "OrderRejected received for unknown order. orderId={}, traceId={}, reason={}",
                    event.orderId(),
                    event.traceId(),
                    event.reason()
            );
            return;
        }

        if (!PAID_STATUS.equals(currentStatus)) {
            log.info(
                    "OrderRejected skipped: order already processed. orderId={}, status={}, traceId={}",
                    event.orderId(),
                    currentStatus,
                    event.traceId()
            );
            return;
        }

        int updatedRows = orderSpringDataRepository.updateStatusIfCurrent(
                event.orderId(),
                PAID_STATUS,
                CANCELLED_STATUS
        );

        if (updatedRows == 0) {
            String actualStatus = orderSpringDataRepository.findStatusById(event.orderId());
            log.info(
                    "OrderRejected skipped due concurrent status change. orderId={}, expectedStatus={}, actualStatus={}, traceId={}",
                    event.orderId(),
                    PAID_STATUS,
                    actualStatus,
                    event.traceId()
            );
            return;
        }

        log.info(
                    "Order rejected by storage. orderId={}, status={}, traceId={}, reason={}",
                    event.orderId(),
                    CANCELLED_STATUS,
                    event.traceId(),
                    event.reason()
        );
    }

    @RabbitHandler(isDefault = true)
    public void onUnsupportedPayload(Object payload) {
        log.warn("Unsupported payload on order approval result queue: {}", payload);
    }
}
