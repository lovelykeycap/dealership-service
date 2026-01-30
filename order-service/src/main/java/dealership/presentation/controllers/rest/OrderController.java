package dealership.presentation.controllers.rest;

import dealership.application.contracts.OrderService;
import dealership.application.items.dtos.CurrentUser;
import dealership.application.items.dtos.SlotRefDto;
import dealership.application.items.operations.order.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    @PostMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PlaceInStockCarOrder.Response placeInStock(
            @RequestBody PlaceInStockBody request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.placeInStockCarOrder(
                new PlaceInStockCarOrder.Request(currentUserFromJwt(jwt), request.carId())
        );
    }

    @PostMapping("/configured")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PlaceConfiguredCarOrder.Response placeConfigured(
            @RequestBody PlaceConfiguredBody request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.placeConfiguredCarOrder(
                new PlaceConfiguredCarOrder.Request(
                        currentUserFromJwt(jwt),
                        request.modelId(),
                        request.selectedSlots()
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@orderSecurity.canReadOrder(#id, authentication)")
    public CheckOrder.Response getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.checkOrder(new CheckOrder.Request(id, currentUserFromJwt(jwt)));
    }

    @GetMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckInStockCarOrderList.Response checkInStockCarOrderList(@AuthenticationPrincipal Jwt jwt) {
        return service.checkInStockCarOrderList(
                new CheckInStockCarOrderList.Request(currentUserFromJwt(jwt))
        );
    }

    @GetMapping("/configured")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckConfiguredCarOrderList.Response checkConfiguredCarOrderList(@AuthenticationPrincipal Jwt jwt) {
        return service.checkConfiguredCarOrderList(
                new CheckConfiguredCarOrderList.Request(currentUserFromJwt(jwt))
        );
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public UpdateOrder.Response update(@RequestBody UpdateOrder.Request request) {
        return service.updateOrder(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@orderSecurity.canCancelOrder(#id, authentication)")
    public DeleteOrder.Response delete(@PathVariable UUID id) {
        return service.deleteOrder(new DeleteOrder.Request(id));
    }

    private CurrentUser currentUserFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Authenticated JWT must be present.");
        }

        String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
        String subject = normalize(jwt.getSubject());
        String name = firstNonBlank(
                normalize(jwt.getClaimAsString("given_name")),
                normalize(jwt.getClaimAsString("name"))
        );
        String surname = normalize(jwt.getClaimAsString("family_name"));
        Set<String> roles = extractRoles(jwt);

        return new CurrentUser(issuer, subject, name, surname, roles);
    }

    private String firstNonBlank(String first, String second) {
        return first != null ? first : second;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<String> extractRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return Set.of();
        }

        Object rolesObj = realmAccessMap.get("roles");
        if (!(rolesObj instanceof Collection<?> rawRoles)) {
            return Set.of();
        }

        LinkedHashSet<String> roles = new LinkedHashSet<>();
        for (Object rawRole : rawRoles) {
            if (!(rawRole instanceof String role)) {
                continue;
            }

            String normalized = normalizeRole(role);
            if (normalized != null) {
                roles.add(normalized);
            }
        }

        return roles;
    }

    private String normalizeRole(String role) {
        String trimmed = normalize(role);
        if (trimmed == null) {
            return null;
        }

        String upper = trimmed.toUpperCase();
        if (upper.startsWith("ROLE_")) {
            upper = upper.substring("ROLE_".length());
        }

        return upper.isBlank() ? null : upper;
    }

    public record PlaceInStockBody(UUID carId) {}

    public record PlaceConfiguredBody(UUID modelId, List<SlotRefDto> selectedSlots) {}
}
