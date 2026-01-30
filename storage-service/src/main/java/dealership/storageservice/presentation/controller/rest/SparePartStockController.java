package dealership.storageservice.presentation.controller.rest;

import dealership.storageservice.application.contracts.SparePartStockService;
import dealership.storageservice.application.items.operations.sparepartstock.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/spare-part-stocks")
@RequiredArgsConstructor
public class SparePartStockController {

    private final SparePartStockService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public AddSparePartStock.Response add(@RequestBody AddSparePartStock.Request request) {
        return service.addSparePartStock(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckSparePartStock.Response getById(@PathVariable UUID id) {
        return service.checkSparePartStock(new CheckSparePartStock.Request(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public CheckSparePartStockList.Response getList() {
        return service.checkSparePartStockList(new CheckSparePartStockList.Request());
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
    public RemoveSparePartStock.Response remove(@RequestBody RemoveSparePartStock.Request request) {
        return service.removeSparePartStock(request);
    }
}

