package dealership.presentation.controllers.rest;

import dealership.application.contracts.TestDriveService;
import dealership.application.items.operations.testdrive.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/test-drives")
public class TestDriveController {
    private final TestDriveService service;

    @PostMapping("/allowed-models")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public AddCarModelToTestDriveList.Response addAllowedModel(
            @RequestBody AddCarModelToTestDriveList.Request request
    ) {
        return service.addCarModelToTestDriveList(request);
    }

    @DeleteMapping("/allowed-models/{modelId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public RemoveCarModelFromTestDriveList.Response removeAllowedModel(@PathVariable UUID modelId) {
        return service.removeCarModelFromTestDriveList(new RemoveCarModelFromTestDriveList.Request(modelId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CreateTestDrive.Response create(@RequestBody CreateTestDrive.Request request) {
        return service.createTestDrive(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CheckTestDrive.Response getById(@PathVariable UUID id) {
        return service.checkTestDrive(new CheckTestDrive.Request(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CheckTestDriveList.Response getList() {
        return service.checkTestDriveList(new CheckTestDriveList.Request());
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public UpdateTestDrive.Response update(@RequestBody UpdateTestDrive.Request request) {
        return service.updateTestDrive(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public DeleteTestDrive.Response delete(@PathVariable UUID id) {
        return service.deleteTestDrive(new DeleteTestDrive.Request(id));
    }
}
