package dealership.storageservice.presentation.controller.rest;

import dealership.storageservice.application.contracts.CarModelService;
import dealership.storageservice.application.items.operations.carmodel.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/car-models")
@RequiredArgsConstructor
public class CarModelController {

    private final CarModelService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public AddCarModel.Response add(@RequestBody AddCarModel.Request request) {
        return service.addCarModel(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','WAREHOUSE_ADMIN','ADMIN')")
    public CheckCarModel.Response getById(@PathVariable UUID id) {
        return service.checkCarModel(new CheckCarModel.Request(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckCarModelList.Response search(@RequestBody CheckCarModelList.Request request) {
        return service.checkCarModelList(request);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public UpdateCarModel.Response update(@RequestBody UpdateCarModel.Request request) {
        return service.updateCarModel(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public DeleteCarModel.Response delete(@PathVariable UUID id) {
        return service.deleteCarModel(new DeleteCarModel.Request(id));
    }
}

