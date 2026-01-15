package dealership.domain.entities.persons;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Manager {
    private final UUID id;

    private final String name;
    private final String surname;
}