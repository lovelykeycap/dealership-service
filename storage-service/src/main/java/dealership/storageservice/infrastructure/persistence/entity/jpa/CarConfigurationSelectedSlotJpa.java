package dealership.storageservice.infrastructure.persistence.entity.jpa;

import dealership.storageservice.infrastructure.persistence.entity.ids.CarConfigurationSelectedSlotJpaId;
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
@Table(name = "car_configuration_selected_slots")
@IdClass(CarConfigurationSelectedSlotJpaId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CarConfigurationSelectedSlotJpa {
    @Id
    @Column(name = "configuration_id", nullable = false)
    private UUID configurationId;

    @Id
    @Column(name = "slot_type", nullable = false, length = 64)
    private String slotType;

    @Column(name = "slot_variant_id", nullable = false)
    private UUID slotVariantId;
}
