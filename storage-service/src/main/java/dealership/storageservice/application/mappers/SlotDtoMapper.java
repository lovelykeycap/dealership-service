package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;

import java.util.List;
import java.util.Map;

public class SlotDtoMapper {
    public List<SlotDto> toSlotDtos(Map<SlotType, SlotVariant> slotVariants) {
        return slotVariants.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(this::toSlotDto)
                .toList();
    }

    public SlotDto toSlotDto(SlotVariant slotVariant) {
        return new SlotDto(
                slotVariant.getId(),
                slotVariant.getSlot().name(),
                slotVariant.getValue()
        );
    }
}

