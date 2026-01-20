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
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpa extends BaseJpa {
    @Column(name = "with_configuration", nullable = false)
    private boolean withConfiguration;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "requested_configuration_id", nullable = false)
    private UUID requestedConfigurationId;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "status", nullable = false, length = 128)
    private String status;

    public OrderJpa(
            UUID id,
            boolean withConfiguration,
            UUID clientId,
            UUID managerId,
            UUID requestedConfigurationId,
            UUID carId,
            String status
    ) {
        super(id);
        this.withConfiguration = withConfiguration;
        this.clientId = clientId;
        this.managerId = managerId;
        this.requestedConfigurationId = requestedConfigurationId;
        this.carId = carId;
        this.status = status;
    }
}

