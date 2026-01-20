package dealership.storageservice.infrastructure.persistence.entity.mappers;

import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.infrastructure.persistence.entity.jpa.SlotVariantJpa;
import dealership.storageservice.infrastructure.persistence.entity.mappers.config.JpaMapperConfig;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = JpaMapperConfig.class)
public interface SlotVariantJpaMapper {

    @InheritConfiguration(name = "toJpaIgnoreAudit")
    @Mapping(target = "slotType", source = "slot")
    @Mapping(target = "price", source = "price")
    SlotVariantJpa toJpa(SlotVariant domain);

    @Mapping(target = "slot", source = "jpa.slotType")
    @Mapping(target = "price", source = "jpa.price")
    @Mapping(
            target = "compatibleCars",
            expression = "java(compatibleCars == null ? java.util.List.of() : compatibleCars)"
    )
    SlotVariant toDomain(SlotVariantJpa jpa, List<CarModel> compatibleCars);

    default String map(SlotType v) { return v == null ? null : v.name(); }
    default SlotType map(String v) { return v == null ? null : SlotType.valueOf(v); }
    default BigDecimal map(Money v) { return v == null ? null : v.amount(); }
    default Money map(BigDecimal v) { return v == null ? null : new Money(v); }
}

