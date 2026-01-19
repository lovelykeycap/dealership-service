package dealership.application.abstractions.persistence;

import java.util.UUID;

public interface SlotVariantStockRepository {
    int getQuantity(UUID slotVariantId);

    boolean hasAtLeast(UUID slotVariantId, int quantity);

    void addQuantity(UUID slotVariantId, int quantity);

    void removeQuantity(UUID slotVariantId, int quantity);
}
