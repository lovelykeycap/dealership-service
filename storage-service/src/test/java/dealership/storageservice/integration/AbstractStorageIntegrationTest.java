package dealership.storageservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "grpc.server.port=0"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractStorageIntegrationTest {

    protected static final UUID IN_STOCK_CAR_ID = UUID.fromString("9d81fc5a-8f74-4ab0-a33e-e4be3d4bf501");
    protected static final UUID BMW_X5_MODEL_ID = UUID.fromString("36a5243a-04ea-4aa2-95b7-f32dbe449301");
    protected static final UUID WHEELS_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a01");
    protected static final UUID TRANSMISSION_SLOT_ID = UUID.fromString("1f01f393-fd0f-4f39-b9ea-32c0a20f0a02");

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_it")
            .withUsername("dealership")
            .withPassword("dealership");

    static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    static {
        postgres.start();
        rabbitmq.start();
    }

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
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    protected RequestPostProcessor authAs(String subject, String... roles) {
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
                        .claim("given_name", "Storage")
                        .claim("family_name", "Tester")
                        .claim("realm_access", Map.of("roles", normalizedRoles))
                )
                .authorities(authorities);
    }
}
