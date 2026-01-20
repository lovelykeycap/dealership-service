package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "car_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarConfigurationJpa extends BaseJpa {
    @Column(name = "model_id", nullable = false)
    private UUID modelId;

    public CarConfigurationJpa(UUID id, UUID modelId) {
        super(id);
        this.modelId = modelId;
    }
}

