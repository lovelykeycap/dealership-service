package dealership.presentation.controllers.exceptions;

import dealership.application.exceptions.StorageServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn503ForStorageUnavailableException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/cars");

        ResponseEntity<ApiError> response = handler.handleStorageUnavailable(
                new StorageServiceUnavailableException("Storage service is unavailable."),
                request
        );

        assertEquals(503, response.getStatusCode().value());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().code());
        assertEquals("/api/v1/cars", response.getBody().path());
    }
}
