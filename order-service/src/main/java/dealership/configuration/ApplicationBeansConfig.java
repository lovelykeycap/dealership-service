package dealership.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.integration.StorageAvailableCarsGateway;
import dealership.application.abstractions.persistence.PersistenceContext;
import dealership.application.items.helpers.OrderChangeApplier;
import dealership.application.items.helpers.TestDriveChangeApplier;
import dealership.application.mappers.OrderDtoMapper;
import dealership.application.mappers.SlotDtoMapper;
import dealership.application.mappers.TestDriveDtoMapper;
import dealership.application.services.AvailableCarsServiceImpl;
import dealership.application.services.OrderServiceImpl;
import dealership.application.services.TestDriveServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public SlotDtoMapper slotDtoMapper() {
        return new SlotDtoMapper();
    }

    @Bean
    public OrderDtoMapper orderDtoMapper(SlotDtoMapper slotDtoMapper) {
        return new OrderDtoMapper(slotDtoMapper);
    }

    @Bean
    public TestDriveDtoMapper testDriveDtoMapper() {
        return new TestDriveDtoMapper();
    }

    @Bean
    public OrderChangeApplier orderChangeApplier() {
        return new OrderChangeApplier();
    }

    @Bean
    public TestDriveChangeApplier testDriveChangeApplier() {
        return new TestDriveChangeApplier();
    }

    @Bean
    public OrderServiceImpl orderService(
            PersistenceContext persistenceContext,
            OrderChangeApplier orderChangeApplier,
            OrderDtoMapper orderDtoMapper,
            ObjectMapper objectMapper,
            StorageCatalogGateway storageCatalogGateway
    ) {
        return new OrderServiceImpl(
                persistenceContext,
                orderChangeApplier,
                orderDtoMapper,
                objectMapper,
                storageCatalogGateway
        );
    }

    @Bean
    public TestDriveServiceImpl testDriveService(
            PersistenceContext persistenceContext,
            TestDriveDtoMapper testDriveDtoMapper,
            TestDriveChangeApplier testDriveChangeApplier,
            StorageCatalogGateway storageCatalogGateway
    ) {
        return new TestDriveServiceImpl(
                persistenceContext,
                testDriveDtoMapper,
                testDriveChangeApplier,
                storageCatalogGateway
        );
    }

    @Bean
    public AvailableCarsServiceImpl availableCarsService(
            StorageAvailableCarsGateway storageAvailableCarsGateway
    ) {
        return new AvailableCarsServiceImpl(storageAvailableCarsGateway);
    }
}
