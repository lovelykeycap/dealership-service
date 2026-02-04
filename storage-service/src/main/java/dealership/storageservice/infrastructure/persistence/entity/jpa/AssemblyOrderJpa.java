package dealership.storageservice.infrastructure.persistence.entity.jpa;

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
@Table(name = "assembly_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssemblyOrderJpa extends BaseJpa {

    @Column(name = "source_order_id", nullable = false, unique = true)
    private UUID sourceOrderId;

    @Column(name = "source_order_type", nullable = false, length = 64)
    private String sourceOrderType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "car_model_id")
    private UUID carModelId;

    @Column(name = "warehouse_manager_id")
    private UUID warehouseManagerId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    public AssemblyOrderJpa(
            UUID id,
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID carModelId,
            UUID warehouseManagerId,
            String status
    ) {
        super(id);
        this.sourceOrderId = sourceOrderId;
        this.sourceOrderType = sourceOrderType;
        this.carId = carId;
        this.carModelId = carModelId;
        this.warehouseManagerId = warehouseManagerId;
        this.status = status;
    }
}
