package dealership.application.abstractions.persistence;

import java.util.Set;
import java.util.UUID;

public interface TestDriveCarModelRepository {
    void addModelId(UUID modelId);

    void removeModelId(UUID modelId);

    boolean containsModelId(UUID modelId);

    Set<UUID> findAllModelIds();
}
