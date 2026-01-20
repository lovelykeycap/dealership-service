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
@Table(name = "slot_variant_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlotVariantStockJpa extends BaseJpa {
    @Column(name = "slot_variant_id", nullable = false, unique = true)
    private UUID slotVariantId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public SlotVariantStockJpa(UUID id, UUID slotVariantId, int quantity) {
        super(id);
        this.slotVariantId = slotVariantId;
        this.quantity = quantity;
    }
}

