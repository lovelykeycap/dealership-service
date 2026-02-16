package dealership.infrastructure.integration.storage;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.cars.CarStockStatus;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.entities.valueobjects.Money;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StorageCatalogHttpGateway implements StorageCatalogGateway {

    private final RestClient restClient;

    public StorageCatalogHttpGateway(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Car getInStockCar(UUID carId) {
        if (carId == null) {
            throw new DomainValidationException("Car id must not be null.");
        }

        InStockCarResponse response = restClient.get()
                .uri("/in-stock-cars/{id}", carId)
                .headers(this::applyBearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    if (clientResponse.getStatusCode().value() == 404) {
                        throw new EntityNotFoundException("Car \"%s\" not found".formatted(carId));
                    }
                    throw new IllegalStateException(
                            "Storage returned client error %s while loading car \"%s\"."
                                    .formatted(clientResponse.getStatusCode().value(), carId)
                    );
                })
                .body(InStockCarResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Storage returned empty response for car \"%s\".".formatted(carId)
            );
        }

        CarModel model = getCarModel(response.modelId());
        Map<SlotType, SlotVariant> selectedSlots = resolveSelectedSlots(response.selectedSlots(), model);
        CarConfiguration configuration = new CarConfiguration(model);
        applySelectedSlotsToConfiguration(configuration, selectedSlots);

        Car car = new Car(response.carId(), configuration);
        car.changeStatus(parseCarStockStatus(response.status()));
        return car;
    }

    private void applySelectedSlotsToConfiguration(
            CarConfiguration configuration,
            Map<SlotType, SlotVariant> selectedSlots
    ) {
        CarModel model = configuration.getModel();

        for (Map.Entry<SlotType, SlotVariant> entry : selectedSlots.entrySet()) {
            SlotType slotType = entry.getKey();
            SlotVariant slotVariant = entry.getValue();

            if (model.getReplaceableSlots().contains(slotType)) {
                configuration.setSlot(slotVariant);
                continue;
            }

            SlotVariant baseVariant = model.getBaseSlots().get(slotType);
            if (baseVariant == null) {
                throw new IllegalStateException(
                        "Storage returned non-base non-replaceable slot \"%s\" for model \"%s\"."
                                .formatted(slotType, model.getId())
                );
            }

            UUID baseVariantId = baseVariant.getId();
            UUID selectedVariantId = slotVariant.getId();
            if (baseVariantId == null || selectedVariantId == null || !baseVariantId.equals(selectedVariantId)) {
                throw new IllegalStateException(
                        "Storage returned illegal override for non-replaceable slot \"%s\" in model \"%s\"."
                                .formatted(slotType, model.getId())
                );
            }
        }
    }

    @Override
    public CarModel getCarModel(UUID carModelId) {
        if (carModelId == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }

        CarModelResponse response = restClient.get()
                .uri("/car-models/{id}", carModelId)
                .headers(this::applyBearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    if (clientResponse.getStatusCode().value() == 404) {
                        throw new EntityNotFoundException("Car model \"%s\" not found".formatted(carModelId));
                    }
                    throw new IllegalStateException(
                            "Storage returned client error %s while loading car model \"%s\"."
                                    .formatted(clientResponse.getStatusCode().value(), carModelId)
                    );
                })
                .body(CarModelResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Storage returned empty response for car model \"%s\".".formatted(carModelId)
            );
        }

        Set<SlotType> replaceableSlots = parseReplaceableSlots(response.replaceableSlots());
        CarModel carModel = new CarModel(
                response.modelId(),
                response.brand(),
                response.name(),
                new Money(nonNullAmount(response.price(), "Car model price must not be null.")),
                Map.of(),
                replaceableSlots
        );

        Map<SlotType, SlotVariant> baseSlots = resolveBaseSlots(response.baseSlots(), carModel);
        carModel.changeBaseSlots(baseSlots);
        return carModel;
    }

    @Override
    public SlotVariant getSparePartAsSlotVariant(UUID sparePartId, CarModel currentModel) {
        if (sparePartId == null) {
            throw new DomainValidationException("Spare part id must not be null.");
        }
        if (currentModel == null) {
            throw new DomainValidationException("Current car model must not be null.");
        }

        SparePartResponse response = restClient.get()
                .uri("/spare-parts/{id}", sparePartId)
                .headers(this::applyBearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    if (clientResponse.getStatusCode().value() == 404) {
                        throw new EntityNotFoundException("Slot variant \"%s\" not found".formatted(sparePartId));
                    }
                    throw new IllegalStateException(
                            "Storage returned client error %s while loading spare part \"%s\"."
                                    .formatted(clientResponse.getStatusCode().value(), sparePartId)
                    );
                })
                .body(SparePartResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Storage returned empty response for spare part \"%s\".".formatted(sparePartId)
            );
        }

        return toSlotVariant(response, currentModel);
    }

    private Map<SlotType, SlotVariant> resolveBaseSlots(List<SlotSummary> baseSlots, CarModel currentModel) {
        if (baseSlots == null || baseSlots.isEmpty()) {
            throw new IllegalStateException(
                    "Car model \"%s\" does not contain base slots.".formatted(currentModel.getId())
            );
        }

        Map<SlotType, SlotVariant> resolved = new LinkedHashMap<>();
        for (SlotSummary baseSlot : baseSlots) {
            if (baseSlot == null || baseSlot.id() == null || baseSlot.slotType() == null) {
                throw new IllegalStateException(
                        "Car model \"%s\" contains invalid base slot reference."
                                .formatted(currentModel.getId())
                );
            }

            SlotVariant baseVariant = getSparePartAsSlotVariant(baseSlot.id(), currentModel);
            if (baseVariant.getSlot() != parseSlotType(baseSlot.slotType())) {
                throw new IllegalStateException(
                        "Base slot type mismatch for model \"%s\" and slot \"%s\"."
                                .formatted(currentModel.getId(), baseSlot.id())
                );
            }

            resolved.put(baseVariant.getSlot(), baseVariant);
        }
        return resolved;
    }

    private Map<SlotType, SlotVariant> resolveSelectedSlots(List<SlotSummary> selectedSlots, CarModel currentModel) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalStateException("In-stock car must contain selected slots.");
        }

        Map<SlotType, SlotVariant> resolved = new LinkedHashMap<>();
        for (SlotSummary selectedSlot : selectedSlots) {
            if (selectedSlot == null || selectedSlot.id() == null || selectedSlot.slotType() == null) {
                throw new IllegalStateException("In-stock car contains invalid selected slot reference.");
            }

            SlotVariant variant = getSparePartAsSlotVariant(selectedSlot.id(), currentModel);
            SlotType expectedSlotType = parseSlotType(selectedSlot.slotType());
            if (variant.getSlot() != expectedSlotType) {
                throw new IllegalStateException(
                        "Selected slot type mismatch for slot \"%s\"."
                                .formatted(selectedSlot.id())
                );
            }

            resolved.put(expectedSlotType, variant);
        }
        return resolved;
    }

    private SlotVariant toSlotVariant(SparePartResponse response, CarModel currentModel) {
        SlotType slotType = parseSlotType(response.slotType());
        Money price = new Money(nonNullAmount(response.price(), "Spare part price must not be null."));

        List<CarModel> compatibleModels = new ArrayList<>();
        if (response.compatibleCarModelIds() != null
                && response.compatibleCarModelIds().contains(currentModel.getId())) {
            compatibleModels.add(currentModel);
        }

        return new SlotVariant(
                response.sparePartId(),
                slotType,
                response.value(),
                price,
                compatibleModels
        );
    }

    private Set<SlotType> parseReplaceableSlots(Collection<String> rawSlotTypes) {
        if (rawSlotTypes == null) {
            return Set.of();
        }

        LinkedHashSet<SlotType> parsed = new LinkedHashSet<>();
        for (String rawSlotType : rawSlotTypes) {
            parsed.add(parseSlotType(rawSlotType));
        }
        return parsed;
    }

    private SlotType parseSlotType(String rawSlotType) {
        if (rawSlotType == null || rawSlotType.isBlank()) {
            throw new IllegalStateException("Slot type must not be blank.");
        }
        try {
            return SlotType.valueOf(
                    rawSlotType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported slot type: " + rawSlotType, ex);
        }
    }

    private CarStockStatus parseCarStockStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalStateException("Car stock status must not be blank.");
        }
        try {
            return CarStockStatus.valueOf(
                    rawStatus.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported car stock status: " + rawStatus, ex);
        }
    }

    private BigDecimal nonNullAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private void applyBearerToken(HttpHeaders headers) {
        String token = resolveBearerToken();
        headers.setBearerAuth(token);
    }

    private String resolveBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user in security context.");
        }

        Object credentials = authentication.getCredentials();
        if (credentials instanceof AbstractOAuth2Token token) {
            return token.getTokenValue();
        }
        if (credentials instanceof String token && !token.isBlank()) {
            return token;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }

        throw new IllegalStateException("Cannot resolve bearer token from security context.");
    }

    private record InStockCarResponse(
            UUID carId,
            UUID modelId,
            String brand,
            String model,
            BigDecimal price,
            String status,
            List<SlotSummary> selectedSlots
    ) {}

    private record CarModelResponse(
            UUID modelId,
            String brand,
            String name,
            BigDecimal price,
            List<SlotSummary> baseSlots,
            Set<String> replaceableSlots
    ) {}

    private record SparePartResponse(
            UUID sparePartId,
            String slotType,
            String value,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {}

    private record SlotSummary(UUID id, String slotType, String name) {}
}
