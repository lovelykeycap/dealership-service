package dealership.integration.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractControllerIT {

    @Test
    void shouldReturnOrderListsAnd404ForUnknownOrder() throws Exception {
        mockMvc.perform(get("/orders/in-stock")
                        .with(authAs("order-user-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray());

        mockMvc.perform(get("/orders/configured")
                        .with(authAs("order-user-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray());

        UUID unknownOrderId = UUID.randomUUID();
        mockMvc.perform(get("/orders/{id}", unknownOrderId)
                        .with(authAs("order-user-1", "USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(delete("/orders/{id}", unknownOrderId).with(authAs("order-user-1", "USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldValidateInvalidOrderRequests() throws Exception {
        ObjectNode invalidInStockRequest = objectMapper.createObjectNode();

        mockMvc.perform(post("/orders/in-stock")
                        .with(authAs("order-user-1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInStockRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION"));

        ObjectNode invalidConfiguredRequest = objectMapper.createObjectNode();
        invalidConfiguredRequest.putArray("selectedSlots");

        mockMvc.perform(post("/orders/configured")
                        .with(authAs("order-user-2", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidConfiguredRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION"));

        ObjectNode invalidUpdateRequest = objectMapper.createObjectNode();
        invalidUpdateRequest.put("orderId", UUID.randomUUID().toString());

        mockMvc.perform(patch("/orders")
                        .with(authAs("order-manager-1", "MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION"));
    }
}
