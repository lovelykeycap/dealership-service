package dealership.storageservice.infrastructure.persistence.entity.mappers;

import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.valueobjects.Money;
import dealership.storageservice.infrastructure.persistence.entity.jpa.CarModelJpa;
import dealership.storageservice.infrastructure.persistence.entity.mappers.config.JpaMapperConfig;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(config = JpaMapperConfig.class)
public interface CarModelJpaMapper {

    @InheritConfiguration(name = "toJpaIgnoreAudit")
    CarModelJpa toJpa(CarModel domain);

    @Mapping(target = "replaceableSlots", ignore = true)
    @Mapping(target = "baseSlots", ignore = true)
    CarModel toDomain(CarModelJpa jpa);

    default BigDecimal map(Money v) { return v == null ? null : v.amount(); }
    default Money map(BigDecimal v) { return v == null ? null : new Money(v); }
}
