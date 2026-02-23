package dealership.integration.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.cars.CarStockStatus;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.EntityNotFoundException;
import dealership.infrastructure.messaging.OutboxPublisher;
import dealership.messaging.contracts.OrderApprovedEvent;
import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.messaging.contracts.RabbitContracts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderApprovalMessagingIT {

    private static final UUID IN_STOCK_CAR_ID = UUID.fromString("9d81fc5a-8f74-4ab0-a33e-e4be3d4bf501");
    private static final UUID BMW_X5_MODEL_ID = UUID.fromString("36a5243a-04ea-4aa2-95b7-f32dbe449301");
    private static final UUID WHEELS_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a01");
    private static final UUID TRANSMISSION_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a02");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_msg_it")
            .withUsername("dealership")
            .withPassword("dealership");

    @Container
    static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);

        registry.add("messaging.outbox.publisher.initial-delay-ms", () -> "3600000");
        registry.add("messaging.outbox.publisher.fixed-delay-ms", () -> "3600000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private StorageCatalogGateway storageCatalogGateway;

    @BeforeEach
    void stubStorageCatalogGateway() {
        CarModel bmwModel = createModel(BMW_X5_MODEL_ID, "BMW", "X5");

        when(storageCatalogGateway.getCarModel(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (BMW_X5_MODEL_ID.equals(id)) {
                return bmwModel;
            }
            throw new EntityNotFoundException("Car model \"%s\" not found".formatted(id));
        });

        when(storageCatalogGateway.getInStockCar(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (IN_STOCK_CAR_ID.equals(id)) {
                Car car = new Car(id, new CarConfiguration(createModel(BMW_X5_MODEL_ID, "BMW", "X5")));
                car.changeStatus(CarStockStatus.AVAILABLE);
                return car;
            }
            throw new EntityNotFoundException("Car \"%s\" not found".formatted(id));
        });

        when(storageCatalogGateway.getSparePartAsSlotVariant(any(UUID.class), any(CarModel.class)))
                .thenAnswer(invocation -> {
                    UUID sparePartId = invocation.getArgument(0);
                    CarModel currentModel = invocation.getArgument(1);
                    SlotType slotType = resolveSlotType(sparePartId, currentModel);
                    return new SlotVariant(
                            sparePartId,
                            slotType,
                            slotType.name() + "-stub",
                            new Money(BigDecimal.ZERO),
                            List.of(currentModel)
                    );
                });
    }

    @Test
    void shouldPublishOutboxEventAndHandleApprovedResult() throws Exception {
        UUID orderId = placeInStockOrder();

        updateOrder(orderId, "markApproved");
        updateOrder(orderId, "markWaitingForPayment");
        updateOrder(orderId, "markPaid");

        Integer newEventsCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from outbox_events
                        where aggregate_id = ?
                          and event_type = 'OrderSentForApprovalEvent'
                          and status = 'NEW'
                        """,
                Integer.class,
                orderId
        );
        assertNotNull(newEventsCount);
        assertTrue(newEventsCount > 0, "Expected NEW outbox event after markPaid.");

        outboxPublisher.publishReadyEvents();

        waitForCondition(
                () -> {
                    String status = jdbcTemplate.queryForObject(
                            "select status from outbox_events where aggregate_id = ? order by created_at desc limit 1",
                            String.class,
                            orderId
                    );
                    return "SENT".equals(status);
                },
                10_000,
                "Outbox event was not marked SENT."
        );

        OrderSentForApprovalEvent sentEvent = waitForSentForApprovalEvent(10_000);
        assertNotNull(sentEvent);
        assertTrue(orderId.equals(sentEvent.orderId()), "Published event must reference created order.");
        assertTrue(sentEvent.traceId() != null && !sentEvent.traceId().isBlank(), "traceId must be present.");

        rabbitTemplate.convertAndSend(
                RabbitContracts.ORDERS_EXCHANGE,
                RabbitContracts.ORDER_APPROVED_ROUTING_KEY,
                new OrderApprovedEvent(
                        orderId,
                        sentEvent.traceId(),
                        UUID.randomUUID(),
                        Instant.now()
                )
        );

        waitForCondition(
                () -> "Ready for pickup".equals(
                        jdbcTemplate.queryForObject(
                                "select status from orders where id = ?",
                                String.class,
                                orderId
                        )
                ),
                10_000,
                "Order status was not updated to 'Ready for pickup'."
        );
    }

    private UUID placeInStockOrder() throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("carId", IN_STOCK_CAR_ID.toString());

        MvcResult result = mockMvc.perform(post("/orders/in-stock")
                        .with(authAs("order-msg-user", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(body.path("orderId").asText());
    }

    private void updateOrder(UUID orderId, String changeType) throws Exception {
        ObjectNode change = objectMapper.createObjectNode();
        change.put("type", changeType);

        ObjectNode request = objectMapper.createObjectNode();
        request.put("orderId", orderId.toString());
        request.set("change", change);

        mockMvc.perform(patch("/orders")
                        .with(authAs("order-msg-manager", "MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private OrderSentForApprovalEvent waitForSentForApprovalEvent(long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            Message message = rabbitTemplate.receive(RabbitContracts.STORAGE_APPROVAL_QUEUE, 250);
            if (message == null) {
                continue;
            }
            return objectMapper.readValue(message.getBody(), OrderSentForApprovalEvent.class);
        }

        throw new AssertionError("Did not receive OrderSentForApprovalEvent from storage approval queue.");
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

    private RequestPostProcessor authAs(String subject, String... roles) {
        var normalizedRoles = Arrays.stream(roles)
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .map(String::toUpperCase)
                .toList();

        GrantedAuthority[] authorities = normalizedRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toArray(GrantedAuthority[]::new);

        return jwt()
                .jwt(jwt -> jwt
                        .issuer("https://it-issuer.example")
                        .subject(subject)
                        .claim("given_name", "Order")
                        .claim("family_name", "Tester")
                        .claim("realm_access", Map.of("roles", normalizedRoles))
                )
                .authorities(authorities);
    }

    private CarModel createModel(UUID modelId, String brand, String name) {
        Map<SlotType, SlotVariant> baseSlots = new EnumMap<>(SlotType.class);
        Set<SlotType> replaceable = EnumSet.noneOf(SlotType.class);

        CarModel model = new CarModel(
                modelId,
                brand,
                name,
                new Money(new BigDecimal("50000")),
                Map.of(),
                Set.of()
        );

        for (SlotType slotType : SlotType.values()) {
            replaceable.add(slotType);
            UUID slotId = switch (slotType) {
                case WHEELS -> WHEELS_SLOT_ID;
                case TRANSMISSION -> TRANSMISSION_SLOT_ID;
                default -> UUID.nameUUIDFromBytes(("stub-" + modelId + "-" + slotType).getBytes());
            };
            baseSlots.put(
                    slotType,
                    new SlotVariant(
                            slotId,
                            slotType,
                            slotType.name() + "-base",
                            new Money(BigDecimal.ZERO),
                            List.of(model)
                    )
            );
        }

        model.changeBaseSlots(baseSlots);
        model.changeReplaceableSlots(replaceable);
        return model;
    }

    private SlotType resolveSlotType(UUID sparePartId, CarModel currentModel) {
        for (Map.Entry<SlotType, SlotVariant> entry : currentModel.getBaseSlots().entrySet()) {
            SlotVariant variant = entry.getValue();
            if (variant != null && sparePartId.equals(variant.getId())) {
                return entry.getKey();
            }
        }

        if (WHEELS_SLOT_ID.equals(sparePartId)) {
            return SlotType.WHEELS;
        }
        if (TRANSMISSION_SLOT_ID.equals(sparePartId)) {
            return SlotType.TRANSMISSION;
        }
        return SlotType.WHEELS;
    }

    @FunctionalInterface
    private interface Check {
        boolean check() throws Exception;
    }
}
