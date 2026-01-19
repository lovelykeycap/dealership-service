package dealership.application.abstractions.persistence;

public interface PersistenceContext {
    OutboxEventRepository getOutboxEventRepository();

    ClientRepository getClientRepository();

    JwtSubRepository getJwtSubRepository();

    ManagerRepository getManagerRepository();

    OrderRepository getOrderRepository();

    TestDriveRequestRepository getTestDriveRequestRepository();

    TestDriveCarModelRepository getTestDriveCarModelRepository();
}
