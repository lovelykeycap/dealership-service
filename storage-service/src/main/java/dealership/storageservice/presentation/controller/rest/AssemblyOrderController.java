package dealership.storageservice.presentation.controller.rest;

import dealership.storageservice.application.contracts.AssemblyOrderService;
import dealership.storageservice.application.items.operations.assemblyorder.AddAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.CheckAssemblyOrderList;
import dealership.storageservice.application.items.operations.assemblyorder.DeleteAssemblyOrder;
import dealership.storageservice.application.items.operations.assemblyorder.UpdateAssemblyOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/assembly-orders")
@RequiredArgsConstructor
public class AssemblyOrderController {

    private final AssemblyOrderService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public AddAssemblyOrder.Response add(@RequestBody AddAssemblyOrder.Request request) {
        return service.addAssemblyOrder(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckAssemblyOrder.Response getById(@PathVariable UUID id) {
        return service.checkAssemblyOrder(new CheckAssemblyOrder.Request(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckAssemblyOrderList.Response getAll() {
        return service.checkAssemblyOrderList(new CheckAssemblyOrderList.Request());
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public UpdateAssemblyOrder.Response update(@RequestBody UpdateAssemblyOrder.Request request) {
        return service.updateAssemblyOrder(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public DeleteAssemblyOrder.Response delete(@PathVariable UUID id) {
        return service.deleteAssemblyOrder(new DeleteAssemblyOrder.Request(id));
    }
}
