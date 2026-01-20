package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "slot_variants")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlotVariantJpa extends BaseJpa {
    @Column(name = "slot_type", nullable = false, length = 64)
    private String slotType;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    public SlotVariantJpa(UUID id, String slotType, String value, BigDecimal price) {
        super(id);
        this.slotType = slotType;
        this.value = value;
        this.price = price;
    }
}

