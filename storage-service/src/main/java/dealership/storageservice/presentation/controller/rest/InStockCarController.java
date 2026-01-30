package dealership.storageservice.presentation.controller.rest;

import dealership.storageservice.application.contracts.InStockCarService;
import dealership.storageservice.application.items.operations.car.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/in-stock-cars")
@RequiredArgsConstructor
public class InStockCarController {

    private final InStockCarService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public AddCar.Response add(@RequestBody AddCar.Request request) {
        return service.addCar(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','WAREHOUSE_ADMIN','ADMIN')")
    public CheckInStockCar.Response getById(@PathVariable UUID id) {
        return service.checkInStockCar(new CheckInStockCar.Request(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckInStockCarList.Response search(@RequestBody CheckInStockCarList.Request request) {
        return service.checkInStockCarList(request);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public UpdateCar.Response update(@RequestBody UpdateCar.Request request) {
        return service.updateCar(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public DeleteCar.Response delete(@PathVariable UUID id) {
        return service.deleteCar(new DeleteCar.Request(id));
    }
}

