package dealership.application.mappers;

import dealership.application.items.dtos.SlotDto;
import dealership.application.items.operations.order.CheckConfiguredCarOrderList;
import dealership.application.items.operations.order.CheckInStockCarOrderList;
import dealership.application.items.operations.order.CheckOrder;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.orders.Order;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OrderDtoMapper {
    private final SlotDtoMapper slotDtoMapper;

    public CheckOrder.Response toCheckOrderResponse(Order order) {
        CarConfiguration requestedConfiguration = order.getRequestedConfiguration();
        UUID carId = null;
        if (order.getCar() != null) {
            carId = order.getCar().getId();
        }

        return new CheckOrder.Response(
                order.getId(),
                order.isWithConfiguration(),
                order.getClientID(),
                order.getManagerID(),
                requestedConfiguration.getModel().getId(),
                carId,
                requestedConfiguration.calculatePrice().amount(),
                order.getStatus(),
                toSelectedSlots(requestedConfiguration)
        );
    }

    public CheckInStockCarOrderList.InStockCarOrderDto toInStockCarOrderDto(Order order) {
        UUID carId = null;
        if (order.getCar() != null) {
            carId = order.getCar().getId();
        }

        return new CheckInStockCarOrderList.InStockCarOrderDto(
                order.getId(),
                order.getClientID(),
                order.getManagerID(),
                carId,
                order.getRequestedConfiguration().calculatePrice().amount(),
                order.getStatus()
        );
    }

    public CheckConfiguredCarOrderList.ConfiguredCarOrderDto toConfiguredCarOrderDto(Order order) {
        CarConfiguration requestedConfiguration = order.getRequestedConfiguration();

        return new CheckConfiguredCarOrderList.ConfiguredCarOrderDto(
                order.getId(),
                order.getClientID(),
                order.getManagerID(),
                requestedConfiguration.getModel().getId(),
                requestedConfiguration.calculatePrice().amount(),
                order.getStatus(),
                toSelectedSlots(requestedConfiguration)
        );
    }

    private List<SlotDto> toSelectedSlots(CarConfiguration configuration) {
        return slotDtoMapper.toSlotDtos(configuration.getSelectedSlots());
    }
}
