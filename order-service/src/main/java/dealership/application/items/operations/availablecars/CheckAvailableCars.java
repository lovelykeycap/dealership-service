package dealership.application.items.operations.availablecars;

import java.util.List;

public final class CheckAvailableCars {

    private CheckAvailableCars() { }

    public record Request() {}

    public record Response(List<CheckAvailableCar.Response> cars) {}
}
