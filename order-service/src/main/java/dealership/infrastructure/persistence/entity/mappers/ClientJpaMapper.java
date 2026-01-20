package dealership.infrastructure.persistence.entity.mappers;

import dealership.domain.entities.persons.Client;
import dealership.infrastructure.persistence.entity.jpa.ClientJpa;
import dealership.infrastructure.persistence.entity.mappers.config.JpaMapperConfig;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = JpaMapperConfig.class)
public interface ClientJpaMapper {

    @InheritConfiguration(name = "toJpaIgnoreAudit")
    ClientJpa toJpa(Client domain);

    Client toDomain(ClientJpa jpa);
}
