package dealership.infrastructure.integration.storage;

import com.google.protobuf.Empty;
import dealership.application.abstractions.integration.StorageAvailableCarsGateway;
import dealership.application.exceptions.StorageServiceUnavailableException;
import dealership.application.items.dtos.SlotDto;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.EntityNotFoundException;
import dealership.grpc.cars.v1.AvailableCar;
import dealership.grpc.cars.v1.CarsAvailabilityGrpcServiceGrpc;
import dealership.grpc.cars.v1.GetAvailableCarByIdRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class StorageAvailableCarsGrpcGateway implements StorageAvailableCarsGateway {

    private static final Logger log = LoggerFactory.getLogger(StorageAvailableCarsGrpcGateway.class);
    private final long timeoutMs;

    @GrpcClient("storage-cars")
    private CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceBlockingStub storageCarsStub;

    public StorageAvailableCarsGrpcGateway(
            @Value("${storage-service.grpc.timeout-ms:5000}") long timeoutMs
    ) {
        if (timeoutMs <= 0) {
            throw new IllegalStateException("storage-service.grpc.timeout-ms must be greater than 0.");
        }
        this.timeoutMs = timeoutMs;
    }

    @Override
    public List<AvailableCarData> getAvailableCars() {
        log.debug("Calling storage gRPC getAvailableCars. timeoutMs={}", timeoutMs);
        try {
            List<AvailableCarData> cars = withDeadline()
                    .getAvailableCars(Empty.getDefaultInstance())
                    .getCarsList()
                    .stream()
                    .map(this::toAvailableCarData)
                    .toList();
            log.debug("storage gRPC getAvailableCars completed. carsCount={}", cars.size());
            return cars;
        } catch (StatusRuntimeException ex) {
            log.warn(
                    "storage gRPC getAvailableCars failed. status={}, description={}",
                    ex.getStatus().getCode(),
                    ex.getStatus().getDescription()
            );
            throw toRuntimeException(ex, null);
        }
    }

    @Override
    public AvailableCarData getAvailableCarById(UUID carId) {
        if (carId == null) {
            throw new DomainValidationException("Car id must not be null.");
        }

        log.debug("Calling storage gRPC getAvailableCarById. carId={}, timeoutMs={}", carId, timeoutMs);
        try {
            AvailableCar grpcCar = withDeadline().getAvailableCarById(
                    GetAvailableCarByIdRequest.newBuilder()
                            .setCarId(carId.toString())
                            .build()
            );
            AvailableCarData result = toAvailableCarData(grpcCar);
            log.debug("storage gRPC getAvailableCarById completed. carId={}", carId);
            return result;
        } catch (StatusRuntimeException ex) {
            log.warn(
                    "storage gRPC getAvailableCarById failed. carId={}, status={}, description={}",
                    carId,
                    ex.getStatus().getCode(),
                    ex.getStatus().getDescription()
            );
            throw toRuntimeException(ex, carId);
        }
    }

    private CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceBlockingStub withDeadline() {
        if (storageCarsStub == null) {
            throw new IllegalStateException("gRPC storage cars stub is not initialized.");
        }
        return storageCarsStub
                .withWaitForReady()
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);
    }

    private AvailableCarData toAvailableCarData(AvailableCar grpcCar) {
        return new AvailableCarData(
                parseUuid(grpcCar.getCarId(), "car_id"),
                parseUuid(grpcCar.getModelId(), "model_id"),
                emptyToNull(grpcCar.getBrand()),
                emptyToNull(grpcCar.getModel()),
                parsePrice(grpcCar.getPrice()),
                emptyToNull(grpcCar.getStatus()),
                grpcCar.getSelectedSlotsList()
                        .stream()
                        .map(slot -> new SlotDto(
                                parseUuid(slot.getSlotId(), "slot_id"),
                                emptyToNull(slot.getSlotType()),
                                emptyToNull(slot.getSlotName())
                        ))
                        .toList()
        );
    }

    private RuntimeException toRuntimeException(StatusRuntimeException ex, UUID requestedCarId) {
        Status.Code code = ex.getStatus().getCode();
        String description = ex.getStatus().getDescription();

        if (code == Status.Code.NOT_FOUND) {
            if (requestedCarId == null) {
                return new EntityNotFoundException("Available car not found.");
            }
            return new EntityNotFoundException("Car \"%s\" not found".formatted(requestedCarId));
        }

        if (code == Status.Code.INVALID_ARGUMENT) {
            return new DomainValidationException(
                    description == null || description.isBlank()
                            ? "Invalid gRPC request for storage cars."
                            : description
            );
        }

        if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
            return new StorageServiceUnavailableException(
                    "Storage service is unavailable.",
                    ex
            );
        }

        return new IllegalStateException(
                "gRPC call to storage cars failed with status \"%s\": %s"
                        .formatted(code, description == null ? "no details" : description),
                ex
        );
    }

    private UUID parseUuid(String rawUuid, String fieldName) {
        if (rawUuid == null || rawUuid.isBlank()) {
            throw new IllegalStateException("gRPC field \"%s\" must not be blank.".formatted(fieldName));
        }
        try {
            return UUID.fromString(rawUuid);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "gRPC field \"%s\" has invalid UUID value \"%s\".".formatted(fieldName, rawUuid),
                    ex
            );
        }
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            throw new IllegalStateException("gRPC field \"price\" must not be blank.");
        }
        try {
            return new BigDecimal(rawPrice);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("gRPC field \"price\" has invalid decimal value.", ex);
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
