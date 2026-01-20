package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "test_drive_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestDriveRequestJpa extends BaseJpa {
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "car_model_id", nullable = false)
    private UUID carModelId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    public TestDriveRequestJpa(UUID id, UUID clientId, UUID carModelId, LocalDateTime startAt) {
        super(id);
        this.clientId = clientId;
        this.carModelId = carModelId;
        this.startAt = startAt;
    }
}

