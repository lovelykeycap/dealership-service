package dealership.application.items.dtos;

import java.util.Set;

public record CurrentUser(
        String issuer,
        String subject,
        String name,
        String surname,
        Set<String> roles
) {
    public CurrentUser(String issuer, String subject, String name, String surname) {
        this(issuer, subject, name, surname, Set.of());
    }
}
