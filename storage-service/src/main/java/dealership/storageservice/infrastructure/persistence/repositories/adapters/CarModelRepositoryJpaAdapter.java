package dealership.storageservice.infrastructure.persistence.repositories.adapters;

import dealership.storageservice.application.abstractions.persistence.CarModelRepository;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelBaseSlotJpa;
import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelReplaceableSlotJpa;
import dealership.storageservice.infrastructure.persistence.entity.mappers.CarModelJpaMapper;
import dealership.storageservice.infrastructure.persistence.entity.mappers.SlotVariantJpaMapper;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelBaseSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelReplaceableSlotSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.CarModelSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.specifications.CarModelJpaSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CarModelRepositoryJpaAdapter implements CarModelRepository {

    private final CarModelSpringDataRepository carModelSpringDataRepository;
    private final CarModelJpaMapper carModelJpaMapper;
    private final CarModelReplaceableSlotSpringDataRepository carModelReplaceableSlotSpringDataRepository;
    private final CarModelBaseSlotSpringDataRepository carModelBaseSlotSpringDataRepository;
    private final SlotVariantSpringDataRepository slotVariantSpringDataRepository;
    private final SlotVariantJpaMapper slotVariantJpaMapper;

    @Override
    @Transactional
    public CarModel save(CarModel entity) {
        var savedJpa = carModelSpringDataRepository.save(carModelJpaMapper.toJpa(entity));

        carModelReplaceableSlotSpringDataRepository.deleteAllByCarModelId(savedJpa.getId());
        carModelBaseSlotSpringDataRepository.deleteAllByCarModelId(savedJpa.getId());

        List<CarModelReplaceableSlotJpa> replaceableSlotJpaList = entity.getReplaceableSlots().stream()
                .map(slotType -> {
                    if (slotType == null) {
                        throw new IllegalStateException("Car model replaceable slot type must not be null.");
                    }
                    return new CarModelReplaceableSlotJpa(savedJpa.getId(), slotType.name());
                })
                .toList();

        carModelReplaceableSlotSpringDataRepository.saveAll(replaceableSlotJpaList);

        List<CarModelBaseSlotJpa> baseSlotJpaList = entity.getBaseSlots().entrySet().stream()
                .map(entry -> {
                    SlotType slotType = entry.getKey();
                    if (slotType == null) {
                        throw new IllegalStateException("Car model base slot type must not be null.");
                    }
                    var slotVariant = entry.getValue();
                    if (slotVariant == null) {
                        throw new IllegalStateException(
                                "Car model base slot variant must not be null for slot type " + slotType
                        );
                    }
                    UUID slotVariantId = slotVariant.getId();
                    if (slotVariantId == null) {
                        throw new IllegalStateException(
                                "Car model base slot variant id must not be null for slot type " + slotType
                        );
                    }
                    return new CarModelBaseSlotJpa(savedJpa.getId(), slotType.name(), slotVariantId);
                })
                .toList();

        carModelBaseSlotSpringDataRepository.saveAll(baseSlotJpaList);

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarModel> findById(UUID id) {
        return carModelSpringDataRepository.findById(id)
                .map(this::assembleDomain);
    }

    private CarModel assembleDomain(dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelJpa carModelJpa) {
        UUID carModelId = carModelJpa.getId();

        Map<SlotType, SlotVariant> baseSlots = carModelBaseSlotSpringDataRepository.findAllByCarModelId(carModelId)
                .stream()
                .collect(Collectors.toMap(
                        row -> parseSlotType(row.getSlotType()),
                        this::resolveSlotVariantForBaseSlot,
                        (left, right) -> {
                            throw new IllegalStateException(
                                    "Duplicate base slot type \"%s\" for car model \"%s\"."
                                            .formatted(left.getSlot(), carModelId)
                            );
                        },
                        LinkedHashMap::new
                ));

        Set<SlotType> replaceableSlots = carModelReplaceableSlotSpringDataRepository.findAllByCarModelId(carModelId)
                .stream()
                .map(CarModelReplaceableSlotJpa::getSlotType)
                .map(this::parseSlotType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new CarModel(
                carModelJpa.getId(),
                carModelJpa.getBrand(),
                carModelJpa.getModel(),
                new Money(carModelJpa.getBasePrice()),
                baseSlots,
                replaceableSlots
        );
    }

    private SlotVariant resolveSlotVariantForBaseSlot(CarModelBaseSlotJpa baseSlotRow) {
        UUID slotVariantId = baseSlotRow.getSlotVariantId();
        return slotVariantSpringDataRepository.findById(slotVariantId)
                .map(jpa -> slotVariantJpaMapper.toDomain(jpa, List.of()))
                .orElseThrow(() -> new IllegalStateException(
                        "Slot variant \"%s\" referenced by car model \"%s\" was not found."
                                .formatted(slotVariantId, baseSlotRow.getCarModelId())
                ));
    }

    private SlotType parseSlotType(String rawSlotType) {
        if (rawSlotType == null || rawSlotType.isBlank()) {
            throw new IllegalStateException("Slot type must not be blank in persistence data.");
        }
        try {
            return SlotType.valueOf(rawSlotType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported slot type in persistence data: " + rawSlotType, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarModel> findAll() {
        return carModelSpringDataRepository.findAll()
                .stream()
                .map(this::assembleDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarModel> findAllByBrandAndBaseComponents(String brand, Set<UUID> slotVariantIds) {
        return carModelSpringDataRepository.findAll(
                        CarModelJpaSpecifications.byBrandAndBaseSlotVariants(brand, slotVariantIds)
                )
                .stream()
                .map(this::assembleDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        carModelReplaceableSlotSpringDataRepository.deleteAllByCarModelId(id);
        carModelBaseSlotSpringDataRepository.deleteAllByCarModelId(id);
        carModelSpringDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return carModelSpringDataRepository.existsById(id);
    }
}
