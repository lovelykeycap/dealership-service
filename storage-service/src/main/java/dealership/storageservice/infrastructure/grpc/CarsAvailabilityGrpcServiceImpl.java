package dealership.storageservice.infrastructure.grpc;

import com.google.protobuf.Empty;
import dealership.grpc.cars.v1.AvailableCar;
import dealership.grpc.cars.v1.CarsAvailabilityGrpcServiceGrpc;
import dealership.grpc.cars.v1.GetAvailableCarByIdRequest;
import dealership.grpc.cars.v1.GetAvailableCarsResponse;
import dealership.grpc.cars.v1.SelectedSlot;
import dealership.storageservice.application.contracts.InStockCarService;
import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.application.items.operations.car.CheckInStockCar;
import dealership.storageservice.application.items.operations.car.CheckInStockCarList;
import dealership.storageservice.domain.entities.cars.CarStockStatus;
import dealership.storageservice.domain.exceptions.EntityNotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@GrpcService
public class CarsAvailabilityGrpcServiceImpl extends CarsAvailabilityGrpcServiceGrpc.CarsAvailabilityGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CarsAvailabilityGrpcServiceImpl.class);
    private static final String AVAILABLE_STATUS = CarStockStatus.AVAILABLE.name();

    private final InStockCarService inStockCarService;

    public CarsAvailabilityGrpcServiceImpl(InStockCarService inStockCarService) {
        this.inStockCarService = inStockCarService;
    }

    @Override
    public void getAvailableCars(Empty request, StreamObserver<GetAvailableCarsResponse> responseObserver) {
        log.debug("gRPC getAvailableCars called.");
        try {
            List<AvailableCar> cars = inStockCarService
                    .checkInStockCarList(new CheckInStockCarList.Request(null))
                    .cars()
                    .stream()
                    .filter(car -> AVAILABLE_STATUS.equals(car.status()))
                    .map(this::toProtoCar)
                    .toList();

            responseObserver.onNext(
                    GetAvailableCarsResponse.newBuilder()
                            .addAllCars(cars)
                            .build()
            );
            responseObserver.onCompleted();
            log.debug("gRPC getAvailableCars completed. carsCount={}", cars.size());
        } catch (Exception ex) {
            log.error("gRPC getAvailableCars failed.", ex);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to fetch available cars.")
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getAvailableCarById(
            GetAvailableCarByIdRequest request,
            StreamObserver<AvailableCar> responseObserver
    ) {
        String rawCarId = request.getCarId();
        log.debug("gRPC getAvailableCarById called. carId={}", rawCarId);

        try {
            if (rawCarId == null || rawCarId.isBlank()) {
                throw new IllegalArgumentException("car_id must not be blank.");
            }

            UUID carId = UUID.fromString(rawCarId);
            CheckInStockCar.Response car = inStockCarService.checkInStockCar(new CheckInStockCar.Request(carId));

            if (!AVAILABLE_STATUS.equals(car.status())) {
                throw new EntityNotFoundException(
                        "Car \"%s\" is not available for sale.".formatted(carId)
                );
            }

            responseObserver.onNext(toProtoCar(car));
            responseObserver.onCompleted();
            log.debug("gRPC getAvailableCarById completed. carId={}", rawCarId);
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        } catch (EntityNotFoundException ex) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(ex.getMessage())
                            .withCause(ex)
                            .asRuntimeException()
            );
        } catch (Exception ex) {
            log.error("gRPC getAvailableCarById failed. carId={}", rawCarId, ex);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to fetch available car by id.")
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }

    private AvailableCar toProtoCar(CheckInStockCarList.InStockCarDto car) {
        return AvailableCar.newBuilder()
                .setCarId(car.carId().toString())
                .setModelId(car.modelId().toString())
                .setBrand(valueOrEmpty(car.brand()))
                .setModel(valueOrEmpty(car.model()))
                .setPrice(formatPrice(car.price()))
                .setStatus(valueOrEmpty(car.status()))
                .addAllSelectedSlots(toProtoSlots(car.selectedSlots()))
                .build();
    }

    private AvailableCar toProtoCar(CheckInStockCar.Response car) {
        return AvailableCar.newBuilder()
                .setCarId(car.carId().toString())
                .setModelId(car.modelId().toString())
                .setBrand(valueOrEmpty(car.brand()))
                .setModel(valueOrEmpty(car.model()))
                .setPrice(formatPrice(car.price()))
                .setStatus(valueOrEmpty(car.status()))
                .addAllSelectedSlots(toProtoSlots(car.selectedSlots()))
                .build();
    }

    private List<SelectedSlot> toProtoSlots(List<SlotDto> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }

        return slots.stream()
                .map(slot -> SelectedSlot.newBuilder()
                        .setSlotId(slot.id().toString())
                        .setSlotType(valueOrEmpty(slot.slotType()))
                        .setSlotName(valueOrEmpty(slot.name()))
                        .build())
                .toList();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "";
        }
        return price.stripTrailingZeros().toPlainString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
