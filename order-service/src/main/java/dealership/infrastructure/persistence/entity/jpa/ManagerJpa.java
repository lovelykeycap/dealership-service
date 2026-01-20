package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "managers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManagerJpa extends BaseJpa {
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "surname", nullable = false, length = 128)
    private String surname;

    public ManagerJpa(UUID id, String name, String surname) {
        super(id);
        this.name = name;
        this.surname = surname;
    }
}
