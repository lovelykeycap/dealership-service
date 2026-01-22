package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.persistence.SlotVariantStockRepository;
import dealership.infrastructure.persistence.entity.jpa.SlotVariantStockJpa;
import dealership.infrastructure.persistence.repositories.jpa.SlotVariantStockSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
public class SlotVariantStockRepositoryJpaAdapter implements SlotVariantStockRepository {

    private final SlotVariantStockSpringDataRepository slotVariantStockSpringDataRepository;

    @Override
    @Transactional(readOnly = true)
    public int getQuantity(UUID slotVariantId) {
        requireSlotVariantId(slotVariantId);
        return slotVariantStockSpringDataRepository.findBySlotVariantId(slotVariantId)
                .map(SlotVariantStockJpa::getQuantity)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAtLeast(UUID slotVariantId, int quantity) {
        requireSlotVariantId(slotVariantId);
        requireNonNegative(quantity);
        return getQuantity(slotVariantId) >= quantity;
    }

    @Override
    @Transactional
    public void addQuantity(UUID slotVariantId, int quantity) {
        requireSlotVariantId(slotVariantId);
        requirePositive(quantity);

        SlotVariantStockJpa stock = slotVariantStockSpringDataRepository.findBySlotVariantId(slotVariantId)
                .orElse(new SlotVariantStockJpa(UUID.randomUUID(), slotVariantId, 0));

        int updatedQuantity;
        try {
            updatedQuantity = Math.addExact(stock.getQuantity(), quantity);
        } catch (ArithmeticException ex) {
            throw new IllegalStateException("Stock quantity overflow for slot variant \"" + slotVariantId + "\".", ex);
        }

        stock.setQuantity(updatedQuantity);
        slotVariantStockSpringDataRepository.save(stock);
    }

    @Override
    @Transactional
    public void removeQuantity(UUID slotVariantId, int quantity) {
        requireSlotVariantId(slotVariantId);
        requirePositive(quantity);

        SlotVariantStockJpa stock = slotVariantStockSpringDataRepository.findBySlotVariantId(slotVariantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Stock for slot variant \"" + slotVariantId + "\" was not found."
                ));

        int currentQuantity = stock.getQuantity();
        if (currentQuantity < quantity) {
            throw new IllegalStateException(
                    "Not enough stock for slot variant \"%s\". Requested: %d, available: %d."
                            .formatted(slotVariantId, quantity, currentQuantity)
            );
        }

        stock.setQuantity(currentQuantity - quantity);
        slotVariantStockSpringDataRepository.save(stock);
    }

    private void requireSlotVariantId(UUID slotVariantId) {
        if (slotVariantId == null) {
            throw new IllegalStateException("Slot variant id must not be null.");
        }
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalStateException("Quantity must be greater than zero.");
        }
    }

    private void requireNonNegative(int quantity) {
        if (quantity < 0) {
            throw new IllegalStateException("Quantity must not be negative.");
        }
    }
}
