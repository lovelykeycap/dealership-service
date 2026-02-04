package dealership.storageservice.infrastructure.persistence.entity.ids;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyOrderRequiredSlotJpaId implements Serializable {
    private UUID assemblyOrderId;
    private UUID slotVariantId;
}
