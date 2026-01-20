package dealership.storageservice.infrastructure.persistence.entity.jpa;

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
@Table(name = "car_models")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarModelJpa extends BaseJpa {
    @Column(name = "brand", nullable = false, length = 128)
    private String brand;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    public CarModelJpa(UUID id, String brand, String model, BigDecimal basePrice) {
        super(id);
        this.brand = brand;
        this.model = model;
        this.basePrice = basePrice;
    }
}

