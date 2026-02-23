package dealership.storageservice.integration.messaging;

import dealership.messaging.contracts.OrderApprovedEvent;
import dealership.messaging.contracts.OrderRejectedEvent;
import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.messaging.contracts.RabbitContracts;
import dealership.messaging.contracts.SourceOrderType;
import dealership.storageservice.infrastructure.messaging.OrderSentForApprovalListener;
import dealership.storageservice.integration.AbstractStorageIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

class OrderSentForApprovalListenerIT extends AbstractStorageIntegrationTest {

    @Autowired
    private OrderSentForApprovalListener listener;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void clearRabbitTemplateInteractions() {
        reset(rabbitTemplate);
    }

    @Test
    void shouldAssembleInStockOrderAndPublishApprovedEvent() throws Exception {
        UUID sourceOrderId = UUID.randomUUID();
        String traceId = UUID.randomUUID().toString();

        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                sourceOrderId,
                traceId,
                SourceOrderType.IN_STOCK,
                IN_STOCK_CAR_ID,
                BMW_X5_MODEL_ID,
                Set.of(),
                Instant.now()
        );

        listener.onOrderSentForApproval(event);

        waitForCondition(
                () -> "ASSEMBLED".equals(
                        jdbcTemplate.queryForObject(
                                "select status from assembly_orders where source_order_id = ?",
                                String.class,
                                sourceOrderId
                        )
                ),
                10_000,
                "Assembly order was not marked ASSEMBLED."
        );

        String carStatus = jdbcTemplate.queryForObject(
                "select status from cars where id = ?",
                String.class,
                IN_STOCK_CAR_ID
        );
        assertEquals("RESERVED", carStatus);

        OrderApprovedEvent approvedEvent = captureApprovedEvent();
        assertEquals(sourceOrderId, approvedEvent.orderId());
        assertEquals(traceId, approvedEvent.traceId());
        assertTrue(approvedEvent.assemblyOrderId() != null, "Approved event must contain assemblyOrderId.");
    }

    @Test
    void shouldMarkFailAndPublishRejectedEventForInvalidConfiguredOrder() throws Exception {
        UUID sourceOrderId = UUID.randomUUID();
        String traceId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "update slot_variant_stocks set quantity = 0 where slot_variant_id = ?",
                WHEELS_SLOT_ID
        );

        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                sourceOrderId,
                traceId,
                SourceOrderType.CONFIGURED,
                null,
                BMW_X5_MODEL_ID,
                Set.of(WHEELS_SLOT_ID),
                Instant.now()
        );

        listener.onOrderSentForApproval(event);

        waitForCondition(
                () -> "FAIL".equals(
                        jdbcTemplate.queryForObject(
                                "select status from assembly_orders where source_order_id = ?",
                                String.class,
                                sourceOrderId
                        )
                ),
                10_000,
                "Assembly order was not marked FAIL."
        );

        OrderRejectedEvent rejectedEvent = captureRejectedEvent();
        assertEquals(sourceOrderId, rejectedEvent.orderId());
        assertEquals(traceId, rejectedEvent.traceId());
        assertTrue(rejectedEvent.reason() != null && !rejectedEvent.reason().isBlank(), "Rejected event must contain reason.");
    }

    private OrderApprovedEvent captureApprovedEvent() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(
                eq(RabbitContracts.ORDERS_EXCHANGE),
                eq(RabbitContracts.ORDER_APPROVED_ROUTING_KEY),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertTrue(payload instanceof OrderApprovedEvent, "Payload must be OrderApprovedEvent.");
        return (OrderApprovedEvent) payload;
    }

    private OrderRejectedEvent captureRejectedEvent() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(
                eq(RabbitContracts.ORDERS_EXCHANGE),
                eq(RabbitContracts.ORDER_REJECTED_ROUTING_KEY),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertTrue(payload instanceof OrderRejectedEvent, "Payload must be OrderRejectedEvent.");
        return (OrderRejectedEvent) payload;
    }

    private void waitForCondition(Check condition, long timeoutMs, String failureMessage) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.check()) {
                return;
            }
            Thread.sleep(200);
        }

        throw new AssertionError(failureMessage);
    }

    @FunctionalInterface
    private interface Check {
        boolean check() throws Exception;
    }
}
