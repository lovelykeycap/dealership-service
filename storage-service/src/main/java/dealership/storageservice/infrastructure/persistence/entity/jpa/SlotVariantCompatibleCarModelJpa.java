package dealership.storageservice.infrastructure.persistence.entity.jpa;

import dealership.storageservice.infrastructure.persistence.entity.ids.SlotVariantCompatibleCarModelJpaId;
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
@Table(name = "slot_variant_compatible_car_models")
@IdClass(SlotVariantCompatibleCarModelJpaId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SlotVariantCompatibleCarModelJpa {
    @Id
    @Column(name = "slot_variant_id", nullable = false)
    private UUID slotVariantId;

    @Id
    @Column(name = "car_model_id", nullable = false)
    private UUID carModelId;
}

