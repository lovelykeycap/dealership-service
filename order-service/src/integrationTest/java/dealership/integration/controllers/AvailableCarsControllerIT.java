package dealership.integration.controllers;

import dealership.application.exceptions.StorageServiceUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvailableCarsControllerIT extends AbstractControllerIT {

    @Test
    void shouldAllowUserManagerAndAdminToReadAvailableCars() throws Exception {
        mockMvc.perform(get("/api/v1/cars").with(authAs("cars-user-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars").isArray());

        mockMvc.perform(get("/api/v1/cars").with(authAs("cars-manager-1", "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars").isArray());

        mockMvc.perform(get("/api/v1/cars").with(authAs("cars-admin-1", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars").isArray());
    }

    @Test
    void shouldReturnCarByIdAndNotFoundForUnknownCar() throws Exception {
        UUID inStockCarId = UUID.fromString("9d81fc5a-8f74-4ab0-a33e-e4be3d4bf501");

        mockMvc.perform(get("/api/v1/cars/{id}", inStockCarId).with(authAs("cars-user-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(inStockCarId.toString()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(get("/api/v1/cars/{id}", UUID.randomUUID()).with(authAs("cars-user-1", "USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldDenyRolesOutsideAllowedSetAndAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/cars").with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnServiceUnavailableWhenStorageGatewayFails() throws Exception {
        when(storageAvailableCarsGateway.getAvailableCars())
                .thenThrow(new StorageServiceUnavailableException("Storage service is unavailable."));

        mockMvc.perform(get("/api/v1/cars").with(authAs("cars-user-1", "USER")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("SERVICE_UNAVAILABLE"));
    }

    @Test
    void shouldReturnEmptyCarsListWhenGatewayReturnsEmptyResult() throws Exception {
        when(storageAvailableCarsGateway.getAvailableCars()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cars").with(authAs("cars-user-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars").isArray())
                .andExpect(jsonPath("$.cars").isEmpty());
    }
}
