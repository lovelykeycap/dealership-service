package dealership.storageservice.application.mappers;

import dealership.storageservice.application.items.dtos.SlotDto;
import dealership.storageservice.application.items.operations.car.CheckInStockCar;
import dealership.storageservice.application.items.operations.car.CheckInStockCarList;
import dealership.storageservice.domain.entities.cars.Car;
import dealership.storageservice.domain.entities.cars.CarModel;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class InStockCarDtoMapper {
    private final SlotDtoMapper slotDtoMapper;

    public CheckInStockCar.Response toCheckInStockCarResponse(Car car) {
        CarModel carModel = car.getConfiguration().getModel();

        return new CheckInStockCar.Response(
                car.getId(),
                carModel.getId(),
                carModel.getBrand(),
                carModel.getModel(),
                car.getPrice().amount(),
                car.getStatus().name(),
                toSelectedSlots(car)
        );
    }

    public CheckInStockCarList.InStockCarDto toInStockCarDto(Car car) {
        CarModel carModel = car.getConfiguration().getModel();

        return new CheckInStockCarList.InStockCarDto(
                car.getId(),
                carModel.getId(),
                carModel.getBrand(),
                carModel.getModel(),
                car.getPrice().amount(),
                car.getStatus().name(),
                toSelectedSlots(car)
        );
    }

    private List<SlotDto> toSelectedSlots(Car car) {
        return slotDtoMapper.toSlotDtos(car.getConfiguration().getSelectedSlots());
    }
}

