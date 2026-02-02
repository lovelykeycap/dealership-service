package dealership.infrastructure.persistence.entity.ids;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JwtSubClientId implements Serializable {
    private String issuer;
    private String subject;
}
