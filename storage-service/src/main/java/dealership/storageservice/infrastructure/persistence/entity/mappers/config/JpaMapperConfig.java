package dealership.storageservice.infrastructure.persistence.entity.mappers.config;

import dealership.storageservice.infrastructure.persistence.entity.jpa.BaseJpa;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface JpaMapperConfig {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    BaseJpa toJpaIgnoreAudit(Object source);
}
