package dealership.storageservice.infrastructure.persistence.repositories.specifications;

import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelBaseSlotJpa;
import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelJpa;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Subquery;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CarModelJpaSpecifications {

    private CarModelJpaSpecifications() {
    }

    public static Specification<CarModelJpa> byBrandAndBaseSlotVariants(String brand, Set<UUID> slotVariantIds) {
        return Specification.where(hasBrand(brand))
                .and(hasAllBaseSlotVariantIds(slotVariantIds));
    }

    public static Specification<CarModelJpa> hasBrand(String brand) {
        return (root, query, cb) -> {
            if (brand == null || brand.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase());
        };
    }

    public static Specification<CarModelJpa> hasAllBaseSlotVariantIds(Set<UUID> slotVariantIds) {
        return (root, query, cb) -> {
            if (slotVariantIds == null || slotVariantIds.isEmpty()) {
                return cb.conjunction();
            }

            Set<UUID> normalizedIds = slotVariantIds.stream()
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (normalizedIds.isEmpty()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }
            query.distinct(true);

            Subquery<UUID> matchingCarModels = query.subquery(UUID.class);
            var baseSlotRoot = matchingCarModels.from(CarModelBaseSlotJpa.class);

            matchingCarModels.select(baseSlotRoot.get("carModelId"));
            matchingCarModels.where(
                    cb.equal(baseSlotRoot.get("carModelId"), root.get("id")),
                    baseSlotRoot.get("slotVariantId").in(normalizedIds)
            );
            matchingCarModels.groupBy(baseSlotRoot.get("carModelId"));
            matchingCarModels.having(
                    cb.equal(cb.countDistinct(baseSlotRoot.get("slotVariantId")), (long) normalizedIds.size())
            );

            return cb.exists(matchingCarModels);
        };
    }
}
