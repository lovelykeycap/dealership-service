package dealership.infrastructure.persistence.entity.jpa;


import dealership.infrastructure.persistence.entity.ids.JwtSubClientId;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "identity_client_binding")
@IdClass(JwtSubClientId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JwtSubClientJpa {

    @Id
    @Column(name = "subject", nullable = false)
    private String subject;

    @Id
    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;
}
