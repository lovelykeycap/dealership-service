package dealership.infrastructure.persistence.entity.mappers;

import dealership.domain.entities.persons.Manager;
import dealership.infrastructure.persistence.entity.jpa.ManagerJpa;
import dealership.infrastructure.persistence.entity.mappers.config.JpaMapperConfig;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = JpaMapperConfig.class)
public interface ManagerJpaMapper {
    @InheritConfiguration(name = "toJpaIgnoreAudit")
    ManagerJpa toJpa(Manager domain);

    Manager toDomain(ManagerJpa jpa);
}
