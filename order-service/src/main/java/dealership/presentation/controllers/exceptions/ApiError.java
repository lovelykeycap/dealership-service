package dealership.presentation.controllers.exceptions;

public record ApiError(
        int status,
        String code,
        String message,
        String path
) {}
