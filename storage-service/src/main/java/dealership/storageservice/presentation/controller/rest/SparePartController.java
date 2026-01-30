package dealership.storageservice.presentation.controller.rest;

import dealership.storageservice.application.contracts.SparePartService;
import dealership.storageservice.application.items.operations.sparepart.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public AddSparePart.Response add(@RequestBody AddSparePart.Request request) {
        return service.addSparePart(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','WAREHOUSE_ADMIN','ADMIN')")
    public CheckSparePart.Response getById(@PathVariable UUID id) {
        return service.checkSparePart(new CheckSparePart.Request(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckSparePartList.Response getList() {
        return service.checkSparePartList(new CheckSparePartList.Request());
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public UpdateSparePart.Response update(@RequestBody UpdateSparePart.Request request) {
        return service.updateSparePart(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public DeleteSparePart.Response delete(@PathVariable UUID id) {
        return service.deleteSparePart(new DeleteSparePart.Request(id));
    }
}

