package dealership.infrastructure.persistence.entity.mappers;

import dealership.infrastructure.persistence.entity.jpa.TestDriveAllowedModelJpa;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TestDriveAllowedModelJpaMapper {
    default UUID toDomain(TestDriveAllowedModelJpa jpa) {
        return jpa == null ? null : jpa.getCarModelId();
    }

    default TestDriveAllowedModelJpa toJpa(UUID carModelId) {
        return carModelId == null ? null : new TestDriveAllowedModelJpa(carModelId);
    }
}
