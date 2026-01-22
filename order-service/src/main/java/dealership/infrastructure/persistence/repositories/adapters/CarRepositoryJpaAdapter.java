package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.CarModelRepository;
import dealership.application.abstractions.persistence.CarRepository;
import dealership.application.abstractions.persistence.SlotVariantRepository;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarStockStatus;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.infrastructure.persistence.entity.jpa.CarConfigurationJpa;
import dealership.infrastructure.persistence.entity.jpa.CarConfigurationSelectedSlotJpa;
import dealership.infrastructure.persistence.entity.jpa.CarJpa;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSelectedSlotSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.CarSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class CarRepositoryJpaAdapter implements CarRepository {

    private final CarSpringDataRepository carSpringDataRepository;
    private final CarConfigurationSpringDataRepository carConfigurationSpringDataRepository;
    private final CarConfigurationSelectedSlotSpringDataRepository carConfigurationSelectedSlotSpringDataRepository;
    private final CarModelRepository carModelRepository;
    private final SlotVariantRepository slotVariantRepository;

    @Override
    @Transactional
    public Car save(Car entity) {
        UUID carId = entity.getId();
        if (carId == null) {
            throw new IllegalStateException("Car id must not be null.");
        }

        UUID configurationId = carSpringDataRepository.findById(carId)
                .map(CarJpa::getConfigurationId)
                .orElse(UUID.randomUUID());

        UUID modelId = entity.getConfiguration().getModel().getId();
        if (modelId == null) {
            throw new IllegalStateException("Car configuration model id must not be null.");
        }

        carConfigurationSpringDataRepository.save(new CarConfigurationJpa(configurationId, modelId));

        carConfigurationSelectedSlotSpringDataRepository.deleteAllByConfigurationId(configurationId);

        List<CarConfigurationSelectedSlotJpa> selectedSlots = entity.getConfiguration().getSelectedSlots().entrySet()
                .stream()
                .map(entry -> {
                    SlotType slotType = entry.getKey();
                    if (slotType == null) {
                        throw new IllegalStateException("Car configuration slot type must not be null.");
                    }

                    var slotVariant = entry.getValue();
                    if (slotVariant == null) {
                        throw new IllegalStateException(
                                "Car configuration slot variant must not be null for slot type " + slotType
                        );
                    }

                    UUID slotVariantId = slotVariant.getId();
                    if (slotVariantId == null) {
                        throw new IllegalStateException(
                                "Car configuration slot variant id must not be null for slot type " + slotType
                        );
                    }

                    return new CarConfigurationSelectedSlotJpa(configurationId, slotType.name(), slotVariantId);
                })
                .toList();

        carConfigurationSelectedSlotSpringDataRepository.saveAll(selectedSlots);

        String status = entity.getStatus() == null ? null : entity.getStatus().name();
        if (status == null || status.isBlank()) {
            throw new IllegalStateException("Car status must not be null.");
        }

        carSpringDataRepository.save(new CarJpa(carId, configurationId, status));
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Car> findById(UUID id) {
        return carSpringDataRepository.findById(id)
                .map(this::assembleCar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return carSpringDataRepository.findAll().stream()
                .map(this::assembleCar)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        carSpringDataRepository.findById(id).ifPresent(carJpa -> {
            UUID configurationId = carJpa.getConfigurationId();

            carSpringDataRepository.deleteById(id);
            carConfigurationSelectedSlotSpringDataRepository.deleteAllByConfigurationId(configurationId);
            carConfigurationSpringDataRepository.deleteById(configurationId);
        });
    }

    @Override
    public boolean existsById(UUID id) {
        return carSpringDataRepository.existsById(id);
    }

    private Car assembleCar(CarJpa carJpa) {
        UUID configurationId = carJpa.getConfigurationId();
        CarConfigurationJpa configurationJpa = carConfigurationSpringDataRepository.findById(configurationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Car configuration \"%s\" referenced by car \"%s\" was not found."
                                .formatted(configurationId, carJpa.getId())
                ));

        var carModel = carModelRepository.findById(configurationJpa.getModelId())
                .orElseThrow(() -> new IllegalStateException(
                        "Car model \"%s\" referenced by configuration \"%s\" was not found."
                                .formatted(configurationJpa.getModelId(), configurationId)
                ));

        CarConfiguration configuration = new CarConfiguration(carModel);

        List<CarConfigurationSelectedSlotJpa> selectedSlotRows =
                carConfigurationSelectedSlotSpringDataRepository.findAllByConfigurationId(configurationId);

        for (CarConfigurationSelectedSlotJpa selectedSlotRow : selectedSlotRows) {
            SlotType slotType = parseSlotType(selectedSlotRow.getSlotType());
            SlotVariant slotVariant = findSlotVariantById(selectedSlotRow.getSlotVariantId());

            if (slotVariant.getSlot() != slotType) {
                throw new IllegalStateException(
                        "Slot type mismatch in configuration \"%s\": row has \"%s\", variant has \"%s\"."
                                .formatted(configurationId, slotType, slotVariant.getSlot())
                );
            }

            if (carModel.getReplaceableSlots().contains(slotType)) {
                configuration.setSlot(slotVariant);
                continue;
            }

            SlotVariant baseVariant = carModel.getBaseSlots().get(slotType);
            if (baseVariant == null) {
                throw new IllegalStateException(
                        "Configuration \"%s\" contains non-base non-replaceable slot \"%s\"."
                                .formatted(configurationId, slotType)
                );
            }

            UUID baseVariantId = baseVariant.getId();
            UUID selectedVariantId = slotVariant.getId();
            if (baseVariantId == null || selectedVariantId == null || !baseVariantId.equals(selectedVariantId)) {
                throw new IllegalStateException(
                        "Configuration \"%s\" has illegal override for non-replaceable slot \"%s\"."
                                .formatted(configurationId, slotType)
                );
            }
        }

        Car car = new Car(carJpa.getId(), configuration);
        car.changeStatus(parseCarStockStatus(carJpa.getStatus()));
        return car;
    }

    private SlotVariant findSlotVariantById(UUID slotVariantId) {
        return slotVariantRepository.findById(slotVariantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Slot variant \"%s\" referenced by car configuration was not found."
                                .formatted(slotVariantId)
                ));
    }

    private SlotType parseSlotType(String rawSlotType) {
        if (rawSlotType == null || rawSlotType.isBlank()) {
            throw new IllegalStateException("Slot type must not be blank in persistence data.");
        }
        try {
            return SlotType.valueOf(
                    rawSlotType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported slot type in persistence data: " + rawSlotType, ex);
        }
    }

    private CarStockStatus parseCarStockStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalStateException("Car status must not be blank in persistence data.");
        }
        try {
            return CarStockStatus.valueOf(
                    rawStatus.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported car status in persistence data: " + rawStatus, ex);
        }
    }
}
