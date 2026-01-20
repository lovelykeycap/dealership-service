package dealership.storageservice.infrastructure.persistence.entity.jpa;

import dealership.storageservice.infrastructure.persistence.entity.ids.CarModelReplaceableSlotJpaId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "car_model_replaceable_slots")
@IdClass(CarModelReplaceableSlotJpaId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CarModelReplaceableSlotJpa {
    @Id
    @Column(name = "car_model_id", nullable = false)
    private UUID carModelId;

    @Id
    @Column(name = "slot_type", nullable = false, length = 64)
    private String slotType;
}

