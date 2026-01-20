package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "test_drive_allowed_models")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TestDriveAllowedModelJpa {
    @Id
    @Column(name = "car_model_id", nullable = false)
    private UUID carModelId;
}
