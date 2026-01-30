package dealership.storageservice.configuration;

import dealership.storageservice.application.abstractions.persistence.CarModelRepository;
import dealership.storageservice.application.abstractions.persistence.CarRepository;
import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.abstractions.persistence.SlotVariantRepository;
import dealership.storageservice.application.abstractions.persistence.SlotVariantStockRepository;
import dealership.storageservice.application.abstractions.persistence.AssemblyOrderRepository;
import dealership.storageservice.infrastructure.persistence.entity.mappers.CarModelJpaMapper;
import dealership.storageservice.infrastructure.persistence.entity.mappers.SlotVariantJpaMapper;
import dealership.storageservice.infrastructure.persistence.repositories.adapters.AssemblyOrderRepositoryJpaAdapter;
import dealership.storageservice.infrastructure.persistence.repositories.adapters.CarModelRepositoryJpaAdapter;
import dealership.storageservice.infrastructure.persistence.repositories.adapters.CarRepositoryJpaAdapter;
import dealership.storageservice.infrastructure.persistence.repositories.adapters.SlotVariantRepositoryJpaAdapter;
import dealership.storageservice.infrastructure.persistence.repositories.adapters.SlotVariantStockRepositoryJpaAdapter;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.AssemblyOrderRequiredSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.AssemblyOrderSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarConfigurationSelectedSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarConfigurationSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelBaseSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelReplaceableSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantCompatibleCarModelSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantStockSpringDataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceBeansConfig {

    @Bean
    public AssemblyOrderRepository assemblyOrderRepository(
            AssemblyOrderSpringDataRepository assemblyOrderSpringDataRepository,
            AssemblyOrderRequiredSlotSpringDataRepository assemblyOrderRequiredSlotSpringDataRepository
    ) {
        return new AssemblyOrderRepositoryJpaAdapter(
                assemblyOrderSpringDataRepository,
                assemblyOrderRequiredSlotSpringDataRepository
        );
    }

    @Bean
    public CarModelRepository carModelRepository(
            CarModelSpringDataRepository carModelSpringDataRepository,
            CarModelJpaMapper carModelJpaMapper,
            CarModelReplaceableSlotSpringDataRepository carModelReplaceableSlotSpringDataRepository,
            CarModelBaseSlotSpringDataRepository carModelBaseSlotSpringDataRepository,
            SlotVariantSpringDataRepository slotVariantSpringDataRepository,
            SlotVariantJpaMapper slotVariantJpaMapper
    ) {
        return new CarModelRepositoryJpaAdapter(
                carModelSpringDataRepository,
                carModelJpaMapper,
                carModelReplaceableSlotSpringDataRepository,
                carModelBaseSlotSpringDataRepository,
                slotVariantSpringDataRepository,
                slotVariantJpaMapper
        );
    }

    @Bean
    public SlotVariantRepository slotVariantRepository(
            SlotVariantSpringDataRepository slotVariantSpringDataRepository,
            SlotVariantCompatibleCarModelSpringDataRepository slotVariantCompatibleCarModelSpringDataRepository,
            SlotVariantJpaMapper slotVariantJpaMapper,
            CarModelRepository carModelRepository
    ) {
        return new SlotVariantRepositoryJpaAdapter(
                slotVariantSpringDataRepository,
                slotVariantCompatibleCarModelSpringDataRepository,
                slotVariantJpaMapper,
                carModelRepository
        );
    }

    @Bean
    public CarRepository carRepository(
            CarSpringDataRepository carSpringDataRepository,
            CarConfigurationSpringDataRepository carConfigurationSpringDataRepository,
            CarConfigurationSelectedSlotSpringDataRepository carConfigurationSelectedSlotSpringDataRepository,
            CarModelRepository carModelRepository,
            SlotVariantRepository slotVariantRepository
    ) {
        return new CarRepositoryJpaAdapter(
                carSpringDataRepository,
                carConfigurationSpringDataRepository,
                carConfigurationSelectedSlotSpringDataRepository,
                carModelRepository,
                slotVariantRepository
        );
    }

    @Bean
    public SlotVariantStockRepository slotVariantStockRepository(
            SlotVariantStockSpringDataRepository slotVariantStockSpringDataRepository
    ) {
        return new SlotVariantStockRepositoryJpaAdapter(slotVariantStockSpringDataRepository);
    }

    @Bean
    public PersistenceContext persistenceContext(
            AssemblyOrderRepository assemblyOrderRepository,
            CarRepository carRepository,
            CarModelRepository carModelRepository,
            SlotVariantRepository slotVariantRepository,
            SlotVariantStockRepository slotVariantStockRepository
    ) {
        return new PersistenceContext() {
            @Override
            public AssemblyOrderRepository getAssemblyOrderRepository() {
                return assemblyOrderRepository;
            }

            @Override
            public CarRepository getCarRepository() {
                return carRepository;
            }

            @Override
            public CarModelRepository getCarModelRepository() {
                return carModelRepository;
            }

            @Override
            public SlotVariantRepository getSlotVariantRepository() {
                return slotVariantRepository;
            }

            @Override
            public SlotVariantStockRepository getSlotVariantStockRepository() {
                return slotVariantStockRepository;
            }
        };
    }
}
