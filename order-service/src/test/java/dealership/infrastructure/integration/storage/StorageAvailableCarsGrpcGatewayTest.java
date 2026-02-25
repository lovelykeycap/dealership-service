package dealership.infrastructure.integration.storage;

import com.google.protobuf.Empty;
import dealership.application.exceptions.StorageServiceUnavailableException;
import dealership.grpc.cars.v1.CarsAvailabilityGrpcServiceGrpc;
import dealership.grpc.cars.v1.GetAvailableCarByIdRequest;
import io.grpc.Status;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageAvailableCarsGrpcGatewayTest {

    @Test
    void shouldMapUnavailableStatusToStorageUnavailableException() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceImplBase() {
                    @Override
                    public void getAvailableCars(
                            Empty request,
                            StreamObserver<dealership.grpc.cars.v1.GetAvailableCarsResponse> responseObserver
                    ) {
                        responseObserver.onError(
                                Status.UNAVAILABLE.withDescription("storage down").asRuntimeException()
                        );
                    }
                })
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();

        try {
            StorageAvailableCarsGrpcGateway gateway = new StorageAvailableCarsGrpcGateway(1500);
            ReflectionTestUtils.setField(
                    gateway,
                    "storageCarsStub",
                    CarsAvailabilityGrpcServiceGrpc.newBlockingStub(channel)
            );

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    gateway::getAvailableCars
            );
            assertEquals(StorageServiceUnavailableException.class, exception.getClass());
            assertEquals("Storage service is unavailable.", exception.getMessage());
        } finally {
            channel.shutdownNow();
            server.shutdownNow();
        }
    }

    @Test
    void shouldMapDeadlineExceededToStorageUnavailableException() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceImplBase() {
                    @Override
                    public void getAvailableCarById(
                            GetAvailableCarByIdRequest request,
                            StreamObserver<dealership.grpc.cars.v1.AvailableCar> responseObserver
                    ) {
                        responseObserver.onError(
                                Status.DEADLINE_EXCEEDED.withDescription("deadline exceeded").asRuntimeException()
                        );
                    }
                })
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();

        try {
            StorageAvailableCarsGrpcGateway gateway = new StorageAvailableCarsGrpcGateway(1500);
            ReflectionTestUtils.setField(
                    gateway,
                    "storageCarsStub",
                    CarsAvailabilityGrpcServiceGrpc.newBlockingStub(channel)
            );

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> gateway.getAvailableCarById(UUID.randomUUID())
            );
            assertEquals(StorageServiceUnavailableException.class, exception.getClass());
            assertEquals("Storage service is unavailable.", exception.getMessage());
        } finally {
            channel.shutdownNow();
            server.shutdownNow();
        }
    }

    @Test
    void shouldReturnEmptyListWhenGrpcReturnsNoCars() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceImplBase() {
                    @Override
                    public void getAvailableCars(
                            Empty request,
                            StreamObserver<dealership.grpc.cars.v1.GetAvailableCarsResponse> responseObserver
                    ) {
                        responseObserver.onNext(
                                dealership.grpc.cars.v1.GetAvailableCarsResponse.newBuilder().build()
                        );
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();

        try {
            StorageAvailableCarsGrpcGateway gateway = new StorageAvailableCarsGrpcGateway(1500);
            ReflectionTestUtils.setField(
                    gateway,
                    "storageCarsStub",
                    CarsAvailabilityGrpcServiceGrpc.newBlockingStub(channel)
            );

            assertTrue(gateway.getAvailableCars().isEmpty());
        } finally {
            channel.shutdownNow();
            server.shutdownNow();
        }
    }
}
