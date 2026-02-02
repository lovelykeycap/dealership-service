package dealership.configuration;

import dealership.application.abstractions.persistence.PersistenceContext;
import dealership.domain.orders.Order;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("orderSecurity")
@AllArgsConstructor
public class OrderSecurity {

    private final PersistenceContext persistenceContext;

    public boolean isOrderOwner(UUID orderId, Authentication authentication) {
        if (orderId == null) {
            return false;
        }

        Optional<UUID> optionalClientId = resolveCurrentClientId(authentication);
        if (optionalClientId.isEmpty()) {
            return false;
        }
        UUID clientId = optionalClientId.get();

        return persistenceContext.getOrderRepository()
                .findById(orderId)
                .map(order -> clientId.equals(order.getClientID()))
                .orElse(false);
    }

    public boolean canReadOrder(UUID orderId, Authentication authentication) {
        if (orderId == null) {
            return false;
        }

        if (hasAnyRole(authentication, "ADMIN", "MANAGER")) {
            return true;
        }

        if (!hasRole(authentication, "USER")) {
            return false;
        }

        Optional<Order> optionalOrder = persistenceContext.getOrderRepository().findById(orderId);
        if (optionalOrder.isEmpty()) {
            return true;
        }

        Optional<UUID> optionalClientId = resolveCurrentClientId(authentication);
        return optionalClientId.isPresent() && optionalClientId.get().equals(optionalOrder.get().getClientID());
    }

    public boolean canCancelOrder(UUID orderId, Authentication authentication) {
        if (orderId == null) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")) {
            return true;
        }

        if (!hasRole(authentication, "USER")) {
            return false;
        }

        Optional<Order> optionalOrder = persistenceContext.getOrderRepository().findById(orderId);
        if (optionalOrder.isEmpty()) {
            return true;
        }

        Optional<UUID> optionalClientId = resolveCurrentClientId(authentication);
        return optionalClientId.isPresent() && optionalClientId.get().equals(optionalOrder.get().getClientID());
    }

    private Optional<UUID> resolveCurrentClientId(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }

        Optional<Jwt> jwt = extractJwt(authentication);
        if (jwt.isEmpty()) {
            return Optional.empty();
        }

        String issuer = jwt.get().getIssuer() == null ? null : jwt.get().getIssuer().toString();
        String subject = jwt.get().getSubject();

        if (isBlank(issuer) || isBlank(subject)) {
            return Optional.empty();
        }

        return persistenceContext.getJwtSubRepository()
                .findClientIdByIssuerAndSubject(issuer, subject);
    }

    private Optional<Jwt> extractJwt(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt);
        }

        Object credentials = authentication.getCredentials();
        if (credentials instanceof Jwt jwt) {
            return Optional.of(jwt);
        }

        return Optional.empty();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        String expected = normalizeRole(role);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null) {
                continue;
            }

            String granted = normalizeRole(authority.getAuthority());
            if (expected.equals(granted)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return normalized;
    }
}
