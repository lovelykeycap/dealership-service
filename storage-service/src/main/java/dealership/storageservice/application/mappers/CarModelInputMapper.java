package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.helpers.SlotTypeParser;
import dealership.storageservice.domain.entities.slots.SlotType;
import dealership.storageservice.domain.entities.slots.SlotVariant;
import dealership.storageservice.domain.exceptions.DomainValidationException;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CarModelInputMapper {
    private final SlotTypeParser slotTypeParser;

    public Map<SlotType, SlotVariant> toBaseSlots(List<SlotVariant> baseSlotVariants) {
        return baseSlotVariants.stream()
                .collect(Collectors.toMap(
                        SlotVariant::getSlot,
                        Function.identity(),
                        (left, right) -> {
                            throw new DomainValidationException(
                                    "Duplicate base slot type \"%s\" is not allowed."
                                            .formatted(left.getSlot())
                            );
                        },
                        LinkedHashMap::new
                ));
    }

    public Set<SlotType> toReplaceableSlots(Set<String> replaceableSlotNames) {
        return replaceableSlotNames.stream()
                .map(slotTypeParser::parse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

