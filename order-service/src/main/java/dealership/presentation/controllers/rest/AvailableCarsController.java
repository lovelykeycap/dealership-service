package dealership.presentation.controllers.rest;

import dealership.application.contracts.AvailableCarsService;
import dealership.application.items.operations.availablecars.CheckAvailableCar;
import dealership.application.items.operations.availablecars.CheckAvailableCars;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/cars")
public class AvailableCarsController {

    private final AvailableCarsService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckAvailableCars.Response getAvailableCars() {
        return service.checkAvailableCars(new CheckAvailableCars.Request());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckAvailableCar.Response getAvailableCarById(@PathVariable UUID id) {
        return service.checkAvailableCar(new CheckAvailableCar.Request(id));
    }
}
