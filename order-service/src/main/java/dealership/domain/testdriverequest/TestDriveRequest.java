package dealership.domain.testdriverequest;

import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.persons.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TestDriveRequest {
    private final UUID id;
    private final Client client;
    private final CarModel carModel;
    private final LocalDateTime startAt;
}
