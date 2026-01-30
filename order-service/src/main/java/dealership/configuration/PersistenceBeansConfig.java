package dealership.configuration;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.persistence.ClientRepository;
import dealership.application.abstractions.persistence.JwtSubRepository;
import dealership.application.abstractions.persistence.ManagerRepository;
import dealership.application.abstractions.persistence.OutboxEventRepository;
import dealership.application.abstractions.persistence.OrderRepository;
import dealership.application.abstractions.persistence.PersistenceContext;
import dealership.application.abstractions.persistence.TestDriveCarModelRepository;
import dealership.application.abstractions.persistence.TestDriveRequestRepository;
import dealership.infrastructure.persistence.entity.mappers.ClientJpaMapper;
import dealership.infrastructure.persistence.entity.mappers.ManagerJpaMapper;
import dealership.infrastructure.persistence.entity.mappers.TestDriveAllowedModelJpaMapper;
import dealership.infrastructure.persistence.repositories.adapters.ClientRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.JwtSubRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.ManagerRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.OrderRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.OutboxEventRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.TestDriveCarModelRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.adapters.TestDriveRequestRepositoryJpaAdapter;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSelectedSlotSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.ClientSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.JwtSubClientSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.ManagerSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.OrderSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.OutboxEventSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.TestDriveAllowedModelSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.TestDriveRequestSpringDataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceBeansConfig {

    @Bean
    public ClientRepository clientRepository(
            ClientSpringDataRepository clientSpringDataRepository,
            ClientJpaMapper clientJpaMapper
    ) {
        return new ClientRepositoryJpaAdapter(clientSpringDataRepository, clientJpaMapper);
    }

    @Bean
    public ManagerRepository managerRepository(
            ManagerSpringDataRepository managerSpringDataRepository,
            ManagerJpaMapper managerJpaMapper
    ) {
        return new ManagerRepositoryJpaAdapter(managerSpringDataRepository, managerJpaMapper);
    }

    @Bean
    public JwtSubRepository jwtSubRepository(
            JwtSubClientSpringDataRepository jwtSubClientSpringDataRepository
    ) {
        return new JwtSubRepositoryJpaAdapter(jwtSubClientSpringDataRepository);
    }

    @Bean
    public OrderRepository orderRepository(
            OrderSpringDataRepository orderSpringDataRepository,
            CarConfigurationSpringDataRepository carConfigurationSpringDataRepository,
            CarConfigurationSelectedSlotSpringDataRepository carConfigurationSelectedSlotSpringDataRepository,
            StorageCatalogGateway storageCatalogGateway
    ) {
        return new OrderRepositoryJpaAdapter(
                orderSpringDataRepository,
                carConfigurationSpringDataRepository,
                carConfigurationSelectedSlotSpringDataRepository,
                storageCatalogGateway
        );
    }

    @Bean
    public OutboxEventRepository outboxEventRepository(
            OutboxEventSpringDataRepository outboxEventSpringDataRepository
    ) {
        return new OutboxEventRepositoryJpaAdapter(outboxEventSpringDataRepository);
    }

    @Bean
    public TestDriveCarModelRepository testDriveCarModelRepository(
            TestDriveAllowedModelSpringDataRepository testDriveAllowedModelSpringDataRepository,
            TestDriveAllowedModelJpaMapper testDriveAllowedModelJpaMapper
    ) {
        return new TestDriveCarModelRepositoryJpaAdapter(
                testDriveAllowedModelSpringDataRepository,
                testDriveAllowedModelJpaMapper
        );
    }

    @Bean
    public TestDriveRequestRepository testDriveRequestRepository(
            TestDriveRequestSpringDataRepository testDriveRequestSpringDataRepository,
            ClientRepository clientRepository,
            StorageCatalogGateway storageCatalogGateway
    ) {
        return new TestDriveRequestRepositoryJpaAdapter(
                testDriveRequestSpringDataRepository,
                clientRepository,
                storageCatalogGateway
        );
    }

    @Bean
    public PersistenceContext persistenceContext(
            OutboxEventRepository outboxEventRepository,
            ClientRepository clientRepository,
            JwtSubRepository jwtSubRepository,
            ManagerRepository managerRepository,
            OrderRepository orderRepository,
            TestDriveRequestRepository testDriveRequestRepository,
            TestDriveCarModelRepository testDriveCarModelRepository
    ) {
        return new PersistenceContext() {
            @Override
            public OutboxEventRepository getOutboxEventRepository() {
                return outboxEventRepository;
            }

            @Override
            public ClientRepository getClientRepository() {
                return clientRepository;
            }

            @Override
            public JwtSubRepository getJwtSubRepository() {
                return jwtSubRepository;
            }

            @Override
            public ManagerRepository getManagerRepository() {
                return managerRepository;
            }

            @Override
            public OrderRepository getOrderRepository() {
                return orderRepository;
            }

            @Override
            public TestDriveRequestRepository getTestDriveRequestRepository() {
                return testDriveRequestRepository;
            }

            @Override
            public TestDriveCarModelRepository getTestDriveCarModelRepository() {
                return testDriveCarModelRepository;
            }
        };
    }
}
