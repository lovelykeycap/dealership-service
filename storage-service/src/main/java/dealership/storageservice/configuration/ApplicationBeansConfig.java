package dealership.storageservice.configuration;

import dealership.storageservice.application.abstractions.persistence.PersistenceContext;
import dealership.storageservice.application.contracts.AssemblyOrderService;
import dealership.storageservice.application.items.helpers.CarChangeApplier;
import dealership.storageservice.application.items.helpers.CarModelChangeApplier;
import dealership.storageservice.application.items.helpers.SlotTypeParser;
import dealership.storageservice.application.items.helpers.SparePartChangeApplier;
import dealership.storageservice.application.items.search.builder.CarSearchPredicateBuilder;
import dealership.storageservice.application.items.search.handlers.BodyTypePredicateHandler;
import dealership.storageservice.application.items.search.handlers.BrandPredicateHandler;
import dealership.storageservice.application.items.search.handlers.ColorPredicateHandler;
import dealership.storageservice.application.items.search.handlers.DriveTypePredicateHandler;
import dealership.storageservice.application.items.search.handlers.EnginePowerPredicateHandler;
import dealership.storageservice.application.items.search.handlers.EngineVolumePredicateHandler;
import dealership.storageservice.application.items.search.handlers.FuelTypePredicateHandler;
import dealership.storageservice.application.items.search.handlers.ModelPredicateHandler;
import dealership.storageservice.application.items.search.handlers.PricePredicateHandler;
import dealership.storageservice.application.items.search.handlers.TransmissionTypePredicateHandler;
import dealership.storageservice.application.mappers.CarModelDtoMapper;
import dealership.storageservice.application.mappers.CarModelInputMapper;
import dealership.storageservice.application.mappers.InStockCarDtoMapper;
import dealership.storageservice.application.mappers.SlotDtoMapper;
import dealership.storageservice.application.mappers.SparePartDtoMapper;
import dealership.storageservice.application.mappers.SparePartStockDtoMapper;
import dealership.storageservice.application.services.AssemblyOrderServiceImpl;
import dealership.storageservice.application.services.CarModelServiceImpl;
import dealership.storageservice.application.services.InStockCarServiceImpl;
import dealership.storageservice.application.services.SparePartServiceImpl;
import dealership.storageservice.application.services.SparePartStockServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public AssemblyOrderService assemblyOrderService(PersistenceContext persistenceContext) {
        return new AssemblyOrderServiceImpl(persistenceContext);
    }

    @Bean
    public SlotTypeParser slotTypeParser() {
        return new SlotTypeParser();
    }

    @Bean
    public SlotDtoMapper slotDtoMapper() {
        return new SlotDtoMapper();
    }

    @Bean
    public CarModelInputMapper carModelInputMapper(SlotTypeParser slotTypeParser) {
        return new CarModelInputMapper(slotTypeParser);
    }

    @Bean
    public CarModelDtoMapper carModelDtoMapper(SlotDtoMapper slotDtoMapper) {
        return new CarModelDtoMapper(slotDtoMapper);
    }

    @Bean
    public InStockCarDtoMapper inStockCarDtoMapper(SlotDtoMapper slotDtoMapper) {
        return new InStockCarDtoMapper(slotDtoMapper);
    }

    @Bean
    public SparePartDtoMapper sparePartDtoMapper() {
        return new SparePartDtoMapper();
    }

    @Bean
    public SparePartStockDtoMapper sparePartStockDtoMapper() {
        return new SparePartStockDtoMapper();
    }

    @Bean
    public CarSearchPredicateBuilder carSearchPredicateBuilder() {
        return new CarSearchPredicateBuilder(List.of(
                new BrandPredicateHandler(),
                new ModelPredicateHandler(),
                new PricePredicateHandler(),
                new BodyTypePredicateHandler(),
                new FuelTypePredicateHandler(),
                new EnginePowerPredicateHandler(),
                new EngineVolumePredicateHandler(),
                new TransmissionTypePredicateHandler(),
                new DriveTypePredicateHandler(),
                new ColorPredicateHandler()
        ));
    }

    @Bean
    public CarModelChangeApplier carModelChangeApplier(CarModelInputMapper carModelInputMapper) {
        return new CarModelChangeApplier(carModelInputMapper);
    }

    @Bean
    public CarChangeApplier carChangeApplier() {
        return new CarChangeApplier();
    }

    @Bean
    public SparePartChangeApplier sparePartChangeApplier(SlotTypeParser slotTypeParser) {
        return new SparePartChangeApplier(slotTypeParser);
    }

    @Bean
    public CarModelServiceImpl carModelService(
            PersistenceContext persistenceContext,
            CarModelDtoMapper carModelDtoMapper,
            CarModelInputMapper carModelInputMapper,
            CarSearchPredicateBuilder carSearchPredicateBuilder,
            CarModelChangeApplier carModelChangeApplier
    ) {
        return new CarModelServiceImpl(
                persistenceContext,
                carModelDtoMapper,
                carModelInputMapper,
                carSearchPredicateBuilder,
                carModelChangeApplier
        );
    }

    @Bean
    public SparePartServiceImpl sparePartService(
            PersistenceContext persistenceContext,
            SlotTypeParser slotTypeParser,
            SparePartDtoMapper sparePartDtoMapper,
            SparePartChangeApplier sparePartChangeApplier
    ) {
        return new SparePartServiceImpl(
                persistenceContext,
                slotTypeParser,
                sparePartDtoMapper,
                sparePartChangeApplier
        );
    }

    @Bean
    public SparePartStockServiceImpl sparePartStockService(
            PersistenceContext persistenceContext,
            SparePartServiceImpl sparePartService,
            SparePartStockDtoMapper sparePartStockDtoMapper
    ) {
        return new SparePartStockServiceImpl(
                persistenceContext,
                sparePartService,
                sparePartStockDtoMapper
        );
    }

    @Bean
    public InStockCarServiceImpl inStockCarService(
            PersistenceContext persistenceContext,
            CarModelServiceImpl carModelService,
            SparePartServiceImpl sparePartService,
            SparePartStockServiceImpl sparePartStockService,
            CarSearchPredicateBuilder carSearchPredicateBuilder,
            InStockCarDtoMapper inStockCarDtoMapper,
            CarChangeApplier carChangeApplier
    ) {
        return new InStockCarServiceImpl(
                persistenceContext,
                carModelService,
                sparePartService,
                sparePartStockService,
                carSearchPredicateBuilder,
                inStockCarDtoMapper,
                carChangeApplier
        );
    }
}
