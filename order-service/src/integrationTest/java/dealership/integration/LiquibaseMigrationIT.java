package dealership.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LiquibaseMigrationIT {

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
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplySchemaAndSeedData() {
        Integer carModelsTableExists = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables " +
                        "where table_schema = 'public' and table_name = 'car_models'",
                Integer.class
        );
        assertNotNull(carModelsTableExists);
        assertEqualsOne(carModelsTableExists);

        Integer seedCarModels = jdbcTemplate.queryForObject(
                "select count(*) from car_models",
                Integer.class
        );
        assertNotNull(seedCarModels);
        assertTrue(seedCarModels > 0);

        Integer seedClients = jdbcTemplate.queryForObject(
                "select count(*) from clients",
                Integer.class
        );
        assertNotNull(seedClients);
        assertTrue(seedClients > 0);
    }

    private void assertEqualsOne(Integer value) {
        assertTrue(value == 1, "Expected exactly one matching table row.");
    }
}
