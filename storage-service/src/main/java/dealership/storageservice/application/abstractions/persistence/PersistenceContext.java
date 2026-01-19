package dealership.storageservice.application.abstractions.persistence;

public interface PersistenceContext {
    AssemblyOrderRepository getAssemblyOrderRepository();

    CarRepository getCarRepository();

    CarModelRepository getCarModelRepository();

    SlotVariantRepository getSlotVariantRepository();

    SlotVariantStockRepository getSlotVariantStockRepository();
}
