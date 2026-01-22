package dealership.storageservice.infrastructure.persistence.repositories.adapters;

import dealership.storageservice.application.abstractions.persistence.CarModelRepository;
import dealership.storageservice.application.abstractions.persistence.SlotVariantRepository;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.infrastructure.persistence.entity.jpa.SlotVariantCompatibleCarModelJpa;
import dealership.storageservice.infrastructure.persistence.entity.mappers.SlotVariantJpaMapper;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantCompatibleCarModelSpringDataRepository;
import dealership.storageservice.infrastructure.persistence.repositories.jpa.SlotVariantSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class SlotVariantRepositoryJpaAdapter implements SlotVariantRepository {

    private final SlotVariantSpringDataRepository slotVariantSpringDataRepository;
    private final SlotVariantCompatibleCarModelSpringDataRepository slotVariantCompatibleCarModelSpringDataRepository;
    private final SlotVariantJpaMapper slotVariantJpaMapper;
    private final CarModelRepository carModelRepository;

    @Override
    @Transactional
    public SlotVariant save(SlotVariant entity) {
        var savedJpa = slotVariantSpringDataRepository.save(slotVariantJpaMapper.toJpa(entity));

        slotVariantCompatibleCarModelSpringDataRepository.deleteAllBySlotVariantId(savedJpa.getId());

        List<CarModel> safeCompatibleCars = entity.getCompatibleCars() == null ? List.of() : entity.getCompatibleCars();
        List<SlotVariantCompatibleCarModelJpa> compatibilityRows = safeCompatibleCars.stream()
                .filter(Objects::nonNull)
                .map(CarModel::getId)
                .filter(Objects::nonNull)
                .distinct()
                .map(modelId -> new SlotVariantCompatibleCarModelJpa(savedJpa.getId(), modelId))
                .toList();

        if (!compatibilityRows.isEmpty()) {
            slotVariantCompatibleCarModelSpringDataRepository.saveAll(compatibilityRows);
        }

        return slotVariantJpaMapper.toDomain(savedJpa, safeCompatibleCars);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SlotVariant> findById(UUID id) {
        return slotVariantSpringDataRepository.findById(id)
                .map(jpa -> slotVariantJpaMapper.toDomain(
                        jpa,
                        loadCompatibleCars(jpa.getId(), new LinkedHashMap<>())
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotVariant> findAll() {
        Map<UUID, CarModel> carModelCache = new LinkedHashMap<>();
        return slotVariantSpringDataRepository.findAll().stream()
                .map(jpa -> slotVariantJpaMapper.toDomain(
                        jpa,
                        loadCompatibleCars(jpa.getId(), carModelCache)
                ))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        slotVariantCompatibleCarModelSpringDataRepository.deleteAllBySlotVariantId(id);
        slotVariantSpringDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return slotVariantSpringDataRepository.existsById(id);
    }

    private List<CarModel> loadCompatibleCars(UUID slotVariantId, Map<UUID, CarModel> carModelCache) {
        return slotVariantCompatibleCarModelSpringDataRepository.findAllBySlotVariantId(slotVariantId).stream()
                .map(SlotVariantCompatibleCarModelJpa::getCarModelId)
                .filter(Objects::nonNull)
                .distinct()
                .map(carModelId -> carModelCache.computeIfAbsent(carModelId, this::findCarModelById))
                .toList();
    }

    private CarModel findCarModelById(UUID carModelId) {
        return carModelRepository.findById(carModelId)
                .orElseThrow(() -> new IllegalStateException(
                        "Car model \"%s\" referenced by slot variant compatibility was not found."
                                .formatted(carModelId)
                ));
    }
}
