package dealership.storageservice.integration.grpc;

import com.google.protobuf.Empty;
import dealership.grpc.cars.v1.AvailableCar;
import dealership.grpc.cars.v1.CarsAvailabilityGrpcServiceGrpc;
import dealership.grpc.cars.v1.GetAvailableCarByIdRequest;
import dealership.grpc.cars.v1.GetAvailableCarsResponse;
import dealership.storageservice.infrastructure.grpc.CarsAvailabilityGrpcServiceImpl;
import dealership.storageservice.integration.AbstractStorageIntegrationTest;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarsAvailabilityGrpcServiceIT extends AbstractStorageIntegrationTest {

    @Autowired
    private CarsAvailabilityGrpcServiceImpl grpcService;

    private Server inProcessServer;
    private ManagedChannel inProcessChannel;
    private CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        inProcessServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(grpcService)
                .build()
                .start();
        inProcessChannel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        stub = CarsAvailabilityGrpcServiceGrpc.newBlockingStub(inProcessChannel);
    }

    @AfterEach
    void tearDown() {
        if (inProcessChannel != null) {
            inProcessChannel.shutdownNow();
        }
        if (inProcessServer != null) {
            inProcessServer.shutdownNow();
        }
    }

    @Test
    void shouldReturnOnlyAvailableCars() {
        setCarStatus("AVAILABLE");

        GetAvailableCarsResponse response = stub.getAvailableCars(Empty.getDefaultInstance());
        assertFalse(response.getCarsList().isEmpty(), "Available cars list must not be empty.");
        assertTrue(
                response.getCarsList().stream().allMatch(car -> "AVAILABLE".equals(car.getStatus())),
                "gRPC method must return only AVAILABLE cars."
        );
        assertTrue(
                response.getCarsList().stream().anyMatch(car -> IN_STOCK_CAR_ID.toString().equals(car.getCarId())),
                "Seed in-stock car must be present in gRPC response."
        );
    }

    @Test
    void shouldReturnCarByIdWhenAvailable() {
        setCarStatus("AVAILABLE");

        AvailableCar response = stub.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder()
                        .setCarId(IN_STOCK_CAR_ID.toString())
                        .build()
        );

        assertEquals(IN_STOCK_CAR_ID.toString(), response.getCarId());
        assertEquals(BMW_X5_MODEL_ID.toString(), response.getModelId());
        assertEquals("AVAILABLE", response.getStatus());
    }

    @Test
    void shouldReturnEmptyListWhenNoCarsAvailable() {
        try {
            setCarStatus("RESERVED");
            GetAvailableCarsResponse response = stub.getAvailableCars(Empty.getDefaultInstance());
            assertTrue(response.getCarsList().isEmpty(), "List must be empty when no AVAILABLE cars exist.");
        } finally {
            setCarStatus("AVAILABLE");
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownCar() {
        UUID unknownCarId = UUID.randomUUID();

        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> stub.getAvailableCarById(
                        GetAvailableCarByIdRequest.newBuilder()
                                .setCarId(unknownCarId.toString())
                                .build()
                )
        );

        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    private void setCarStatus(String status) {
        jdbcTemplate.update("update cars set status = ? where id = ?", status, IN_STOCK_CAR_ID);
    }
}
