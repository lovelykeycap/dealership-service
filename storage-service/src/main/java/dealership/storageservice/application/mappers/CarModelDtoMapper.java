package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModel;
import dealership.storageservice.application.items.operations.carmodel.CheckCarModelList;
import dealership.storageservice.domain.entities.cars.CarModel;
import dealership.storageservice.domain.entities.slots.SlotType;
import lombok.AllArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CarModelDtoMapper {
    private final SlotDtoMapper slotDtoMapper;

    public CheckCarModel.Response toCheckCarModelResponse(CarModel carModel) {
        return new CheckCarModel.Response(
                carModel.getId(),
                carModel.getBrand(),
                carModel.getModel(),
                carModel.getBasePrice().amount(),
                toBaseSlots(carModel),
                toReplaceableSlots(carModel)
        );
    }

    public CheckCarModelList.CarModelDto toCarModelDto(CarModel carModel) {
        return new CheckCarModelList.CarModelDto(
                carModel.getId(),
                carModel.getBrand(),
                carModel.getModel(),
                carModel.getBasePrice().amount(),
                toBaseSlots(carModel),
                toReplaceableSlots(carModel)
        );
    }

    private List<SlotDto> toBaseSlots(CarModel carModel) {
        return slotDtoMapper.toSlotDtos(carModel.getBaseSlots());
    }

    private Set<String> toReplaceableSlots(CarModel carModel) {
        return carModel.getReplaceableSlots()
                .stream()
                .map(SlotType::name)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

