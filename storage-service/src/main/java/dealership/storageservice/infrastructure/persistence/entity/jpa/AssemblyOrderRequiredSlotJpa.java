package dealership.storageservice.infrastructure.persistence.entity.jpa;

import dealership.storageservice.infrastructure.persistence.entity.ids.AssemblyOrderRequiredSlotJpaId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "assembly_order_required_slots")
@IdClass(AssemblyOrderRequiredSlotJpaId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssemblyOrderRequiredSlotJpa {

    @Id
    @Column(name = "assembly_order_id", nullable = false)
    private UUID assemblyOrderId;

    @Id
    @Column(name = "slot_variant_id", nullable = false)
    private UUID slotVariantId;

    public AssemblyOrderRequiredSlotJpa(UUID assemblyOrderId, UUID slotVariantId) {
        this.assemblyOrderId = assemblyOrderId;
        this.slotVariantId = slotVariantId;
    }
}
