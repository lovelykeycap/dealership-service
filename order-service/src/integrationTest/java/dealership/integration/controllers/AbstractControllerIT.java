package dealership.integration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.integration.StorageAvailableCarsGateway;
import dealership.application.items.dtos.SlotDto;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.cars.CarStockStatus;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class AbstractControllerIT {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dealership_it")
            .withUsername("dealership")
            .withPassword("dealership");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    protected static final UUID CLIENT_1_ID = UUID.fromString("8c45a402-3b09-4cf8-9c7d-7b8f6f5e1a01");
    protected static final UUID CLIENT_2_ID = UUID.fromString("8c45a402-3b09-4cf8-9c7d-7b8f6f5e1a02");
    protected static final UUID MANAGER_1_ID = UUID.fromString("7d3b5f33-7428-4e95-9dd9-2f26f0a7b101");
    protected static final UUID MANAGER_2_ID = UUID.fromString("7d3b5f33-7428-4e95-9dd9-2f26f0a7b102");
    protected static final UUID BMW_X5_MODEL_ID = UUID.fromString("36a5243a-04ea-4aa2-95b7-f32dbe449301");
    protected static final UUID AUDI_Q7_MODEL_ID = UUID.fromString("36a5243a-04ea-4aa2-95b7-f32dbe449302");
    protected static final UUID WHEELS_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a01");
    protected static final UUID TRANSMISSION_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a02");

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockBean
    protected StorageCatalogGateway storageCatalogGateway;

    @MockBean
    protected StorageAvailableCarsGateway storageAvailableCarsGateway;

    @BeforeEach
    void stubStorageCatalogGateway() {
        UUID inStockCarId = UUID.fromString("9d81fc5a-8f74-4ab0-a33e-e4be3d4bf501");
        CarModel bmwModel = createModel(BMW_X5_MODEL_ID, "BMW", "X5");
        CarModel audiModel = createModel(AUDI_Q7_MODEL_ID, "Audi", "Q7");

        when(storageCatalogGateway.getCarModel(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (BMW_X5_MODEL_ID.equals(id)) {
                return bmwModel;
            }
            if (AUDI_Q7_MODEL_ID.equals(id)) {
                return audiModel;
            }
            throw new EntityNotFoundException("Car model \"%s\" not found".formatted(id));
        });

        when(storageCatalogGateway.getInStockCar(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (inStockCarId.equals(id)) {
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

        StorageAvailableCarsGateway.AvailableCarData availableCar = new StorageAvailableCarsGateway.AvailableCarData(
                inStockCarId,
                BMW_X5_MODEL_ID,
                "BMW",
                "X5",
                new BigDecimal("50000"),
                CarStockStatus.AVAILABLE.name(),
                List.of(new SlotDto(WHEELS_SLOT_ID, SlotType.WHEELS.name(), "WHEELS-base"))
        );

        when(storageAvailableCarsGateway.getAvailableCars()).thenReturn(List.of(availableCar));
        when(storageAvailableCarsGateway.getAvailableCarById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (inStockCarId.equals(id)) {
                return availableCar;
            }
            throw new EntityNotFoundException("Car \"%s\" not found".formatted(id));
        });
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected UUID uuidField(MvcResult result, String field) throws Exception {
        JsonNode json = body(result);
        JsonNode value = json.path(field);
        assertTrue(value.isTextual(), "Field '" + field + "' must be textual UUID.");
        return UUID.fromString(value.asText());
    }

    protected RequestPostProcessor authAs(String subject, String... roles) {
        return authAs(subject, "Test", "User", roles);
    }

    protected RequestPostProcessor authAs(String subject, String givenName, String familyName, String... roles) {
        List<String> normalizedRoles = Arrays.stream(roles)
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
                        .claim("given_name", givenName)
                        .claim("family_name", familyName)
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
}
