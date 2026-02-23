package dealership.integration;

import dealership.infrastructure.persistence.entity.jpa.ClientJpa;
import dealership.infrastructure.persistence.entity.jpa.SlotVariantJpa;
import dealership.infrastructure.persistence.entity.jpa.SlotVariantStockJpa;
import dealership.infrastructure.persistence.repositories.jpa.ClientSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.SlotVariantSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.SlotVariantStockSpringDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRepositoriesIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dealership_it")
            .withUsername("dealership")
            .withPassword("dealership");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired
    private ClientSpringDataRepository clientRepository;

    @Autowired
    private SlotVariantSpringDataRepository slotVariantRepository;

    @Autowired
    private SlotVariantStockSpringDataRepository slotVariantStockRepository;

    @Test
    void shouldPersistClientAndAutoFillAuditFields() {
        UUID clientId = UUID.randomUUID();
        ClientJpa toSave = new ClientJpa(clientId, "Integration", "User");

        ClientJpa saved = clientRepository.save(toSave);
        ClientJpa fromDb = clientRepository.findById(clientId).orElseThrow();

        assertEquals(clientId, fromDb.getId());
        assertEquals("Integration", fromDb.getName());
        assertEquals("User", fromDb.getSurname());
        assertNotNull(fromDb.getCreatedAt());
        assertNotNull(fromDb.getUpdatedAt());
        assertFalse(fromDb.isRemoved());
        assertEquals(saved.getId(), fromDb.getId());

        clientRepository.deleteById(clientId);
    }

    @Test
    void shouldFindStockBySlotVariantId() {
        UUID slotVariantId = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();

        SlotVariantJpa slotVariant = new SlotVariantJpa(
                slotVariantId,
                "WHEELS",
                "IT wheels",
                new BigDecimal("1100.00")
        );
        slotVariantRepository.save(slotVariant);

        SlotVariantStockJpa stock = new SlotVariantStockJpa(stockId, slotVariantId, 7);
        slotVariantStockRepository.save(stock);

        SlotVariantStockJpa found = slotVariantStockRepository.findBySlotVariantId(slotVariantId).orElseThrow();

        assertEquals(stockId, found.getId());
        assertEquals(slotVariantId, found.getSlotVariantId());
        assertEquals(7, found.getQuantity());

        slotVariantStockRepository.deleteById(stockId);
        slotVariantRepository.deleteById(slotVariantId);
    }
}
