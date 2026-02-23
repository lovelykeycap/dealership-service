package dealership.storageservice.integration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dealership.storageservice.integration.AbstractStorageIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssemblyOrderControllerIT extends AbstractStorageIntegrationTest {

    @Test
    void shouldSupportAssemblyOrderCrudForWarehouseAdmin() throws Exception {
        UUID sourceOrderId = UUID.randomUUID();
        UUID warehouseManagerId = UUID.randomUUID();

        ObjectNode createBody = objectMapper.createObjectNode();
        createBody.put("sourceOrderId", sourceOrderId.toString());
        createBody.put("sourceOrderType", "IN_STOCK");
        createBody.put("carId", IN_STOCK_CAR_ID.toString());
        createBody.putNull("carModelId");
        createBody.putArray("requiredSlotVariantIds");
        createBody.putNull("warehouseManagerId");

        MvcResult createResult = mockMvc.perform(post("/assembly-orders")
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrderId").isString())
                .andReturn();

        UUID assemblyOrderId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString())
                        .path("assemblyOrderId")
                        .asText()
        );

        mockMvc.perform(get("/assembly-orders/{id}", assemblyOrderId)
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrderId").value(assemblyOrderId.toString()))
                .andExpect(jsonPath("$.sourceOrderId").value(sourceOrderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));

        ObjectNode assignManagerChange = objectMapper.createObjectNode();
        assignManagerChange.put("type", "assignWarehouseManager");
        assignManagerChange.put("warehouseManagerId", warehouseManagerId.toString());

        ObjectNode assignManagerBody = objectMapper.createObjectNode();
        assignManagerBody.put("assemblyOrderId", assemblyOrderId.toString());
        assignManagerBody.set("change", assignManagerChange);

        mockMvc.perform(patch("/assembly-orders")
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignManagerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrderId").value(assemblyOrderId.toString()));

        ObjectNode changeStatusChange = objectMapper.createObjectNode();
        changeStatusChange.put("type", "changeStatus");
        changeStatusChange.put("status", "ASSEMBLED");

        ObjectNode changeStatusBody = objectMapper.createObjectNode();
        changeStatusBody.put("assemblyOrderId", assemblyOrderId.toString());
        changeStatusBody.set("change", changeStatusChange);

        mockMvc.perform(patch("/assembly-orders")
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeStatusBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrderId").value(assemblyOrderId.toString()));

        MvcResult getAfterUpdateResult = mockMvc.perform(get("/assembly-orders/{id}", assemblyOrderId)
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehouseManagerId").value(warehouseManagerId.toString()))
                .andExpect(jsonPath("$.status").value("ASSEMBLED"))
                .andReturn();

        JsonNode getAfterUpdateBody = objectMapper.readTree(getAfterUpdateResult.getResponse().getContentAsString());
        assertEquals(assemblyOrderId.toString(), getAfterUpdateBody.path("assemblyOrderId").asText());

        MvcResult listResult = mockMvc.perform(get("/assembly-orders")
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrders").isArray())
                .andReturn();

        JsonNode ordersArray = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("assemblyOrders");
        boolean containsCreatedOrder = false;
        for (JsonNode orderNode : ordersArray) {
            if (assemblyOrderId.toString().equals(orderNode.path("assemblyOrderId").asText())) {
                containsCreatedOrder = true;
                break;
            }
        }
        assertTrue(containsCreatedOrder, "Assembly order must be present in list.");

        mockMvc.perform(delete("/assembly-orders/{id}", assemblyOrderId)
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assemblyOrderId").value(assemblyOrderId.toString()));

        mockMvc.perform(get("/assembly-orders/{id}", assemblyOrderId)
                        .with(authAs("warehouse-admin-1", "WAREHOUSE_ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldDenyUserRoleForAssemblyOrderEndpoints() throws Exception {
        mockMvc.perform(get("/assembly-orders")
                        .with(authAs("regular-user-1", "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
