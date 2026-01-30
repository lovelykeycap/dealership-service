package dealership.storageservice.presentation.controller.exceptions;

public record ApiError(
        int status,
        String code,
        String message,
        String path
) {}
