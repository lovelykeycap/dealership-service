package dealership.integration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestDriveControllerIT extends AbstractControllerIT {

    @Test
    void shouldCreateGetListAndDeleteTestDrive() throws Exception {
        ObjectNode createRequest = objectMapper.createObjectNode();
        createRequest.put("clientId", CLIENT_1_ID.toString());
        createRequest.put("modelId", BMW_X5_MODEL_ID.toString());
        createRequest.put("startAt", "2030-01-10T10:00:00");

        MvcResult createResult = mockMvc.perform(post("/test-drives")
                        .with(authAs("client-subject-1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andReturn();
        UUID testDriveId = uuidField(createResult, "id");

        mockMvc.perform(get("/test-drives/{id}", testDriveId).with(authAs("manager-subject-1", "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.modelId").value(BMW_X5_MODEL_ID.toString()))
                .andExpect(jsonPath("$.startAt").value("2030-01-10T10:00:00"));

        MvcResult listResult = mockMvc.perform(get("/test-drives").with(authAs("manager-subject-1", "MANAGER")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode requests = body(listResult).path("requests");
        boolean containsTestDrive = false;
        for (JsonNode request : requests) {
            if (testDriveId.toString().equals(request.path("id").asText())) {
                containsTestDrive = true;
                break;
            }
        }
        assertTrue(containsTestDrive, "Test drive list must contain created request.");

        mockMvc.perform(delete("/test-drives/{id}", testDriveId).with(authAs("manager-subject-1", "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()));

        mockMvc.perform(get("/test-drives/{id}", testDriveId).with(authAs("manager-subject-1", "MANAGER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRemoveAndAddAllowedModel() throws Exception {
        mockMvc.perform(delete("/test-drives/allowed-models/{modelId}", AUDI_Q7_MODEL_ID).with(authAs("manager-subject-1", "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId").value(AUDI_Q7_MODEL_ID.toString()));

        ObjectNode addAllowedModelRequest = objectMapper.createObjectNode();
        addAllowedModelRequest.put("modelId", AUDI_Q7_MODEL_ID.toString());

        mockMvc.perform(post("/test-drives/allowed-models")
                        .with(authAs("manager-subject-1", "MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addAllowedModelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId").value(AUDI_Q7_MODEL_ID.toString()));
    }
}
