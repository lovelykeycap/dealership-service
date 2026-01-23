package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.operations.sparepart.CheckSparePart;
import dealership.storageservice.application.items.operations.sparepart.CheckSparePartList;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotVariant;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SparePartDtoMapper {
    public CheckSparePart.Response toCheckSparePartResponse(SlotVariant slotVariant) {
        return new CheckSparePart.Response(
                slotVariant.getId(),
                slotVariant.getSlot().name(),
                slotVariant.getValue(),
                slotVariant.getPrice().amount(),
                toCompatibleCarModelIds(slotVariant)
        );
    }

    public CheckSparePartList.SparePartDto toSparePartDto(SlotVariant slotVariant) {
        return new CheckSparePartList.SparePartDto(
                slotVariant.getId(),
                slotVariant.getSlot().name(),
                slotVariant.getValue(),
                slotVariant.getPrice().amount(),
                toCompatibleCarModelIds(slotVariant)
        );
    }

    private Set<UUID> toCompatibleCarModelIds(SlotVariant slotVariant) {
        return slotVariant.getCompatibleCars()
                .stream()
                .map(CarModel::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

