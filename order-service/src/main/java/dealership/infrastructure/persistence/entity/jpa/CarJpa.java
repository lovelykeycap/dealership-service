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
@Table(name = "cars")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarJpa extends BaseJpa {
    @Column(name = "configuration_id", nullable = false)
    private UUID configurationId;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    public CarJpa(UUID id, UUID configurationId, String status) {
        super(id);
        this.configurationId = configurationId;
        this.status = status;
    }
}

