package dealership.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.persistence.PersistenceContext;
import dealership.application.contracts.OrderService;
import dealership.application.items.dtos.CurrentUser;
import dealership.application.items.dtos.SlotRefDto;
import dealership.application.items.events.NewOutboxEvent;
import dealership.application.items.helpers.OrderChangeApplier;
import dealership.application.items.operations.order.*;
import dealership.domain.configuration.CarConfiguration;
import dealership.application.mappers.OrderDtoMapper;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.cars.CarModel;
import dealership.domain.entities.cars.CarStockStatus;
import dealership.domain.entities.persons.Client;
import dealership.domain.entities.persons.Manager;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.exceptions.DomainValidationException;
import dealership.domain.exceptions.EntityNotFoundException;
import dealership.domain.orders.Order;
import dealership.messaging.contracts.OrderSentForApprovalEvent;
import dealership.messaging.contracts.RabbitContracts;
import dealership.messaging.contracts.SourceOrderType;
import lombok.AllArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final PersistenceContext persistenceContext;
    private final OrderChangeApplier orderChangeApplier;
    private final OrderDtoMapper orderDtoMapper;
    private final ObjectMapper objectMapper;
    private final StorageCatalogGateway storageCatalogGateway;

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PlaceInStockCarOrder.Response placeInStockCarOrder(PlaceInStockCarOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Place in-stock car order request must not be null.");
        }
        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null.");
        }
        validateCurrentUserForPlacement(request.currentUser());

        UUID clientId = findOrCreateClientBySubAndIssuer(request.currentUser());
        Client client = findClientById(clientId);
        Manager manager = resolveManagerForPlacement();
        Car car = findCarById(request.carId());

        if (car.getStatus() != CarStockStatus.AVAILABLE) {
            throw new DomainValidationException(
                    "Car \"%s\" is not available for placing an in-stock order."
                            .formatted(car.getId())
            );
        }

        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                orderId,
                false,
                client.getId(),
                manager.getId(),
                car.getConfiguration(),
                car
        );

        persistenceContext.getOrderRepository().save(order);

        return new PlaceInStockCarOrder.Response(orderId);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PlaceConfiguredCarOrder.Response placeConfiguredCarOrder(PlaceConfiguredCarOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Place configured car order request must not be null.");
        }
        if (request.modelId() == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (request.selectedSlots() == null) {
            throw new DomainValidationException("Selected slots must not be null.");
        }
        validateCurrentUserForPlacement(request.currentUser());

        UUID clientId = findOrCreateClientBySubAndIssuer(request.currentUser());
        Client client = findClientById(clientId);
        Manager manager = resolveManagerForPlacement();
        var configuration = createConfiguration(request.modelId(), request.selectedSlots());

        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                orderId,
                true,
                client.getId(),
                manager.getId(),
                configuration,
                null
        );

        persistenceContext.getOrderRepository().save(order);
        return new PlaceConfiguredCarOrder.Response(orderId);
    }

    @Override
    @PreAuthorize("#request != null and @orderSecurity.canReadOrder(#request.orderId, authentication)")
    public CheckOrder.Response checkOrder(CheckOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check order request must not be null.");
        }
        if (request.orderId() == null) {
            throw new DomainValidationException("Order id must not be null.");
        }
        validateCurrentUserForRead(request.currentUser());

        Order order = findOrderById(request.orderId());
        if (isPrivilegedForOrderRead(request.currentUser())) {
            return orderDtoMapper.toCheckOrderResponse(order);
        }

        UUID currentClientId = findClientIdBySubAndIssuer(request.currentUser())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Order \"%s\" not found".formatted(request.orderId())
                        )
                );
        if (!order.getClientID().equals(currentClientId)) {
            throw new EntityNotFoundException(
                    "Order \"%s\" not found".formatted(request.orderId())
            );
        }

        return orderDtoMapper.toCheckOrderResponse(order);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckInStockCarOrderList.Response checkInStockCarOrderList(CheckInStockCarOrderList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check in-stock car order list request must not be null.");
        }
        validateCurrentUserForRead(request.currentUser());

        if (isPrivilegedForOrderRead(request.currentUser())) {
            var orders = persistenceContext.getOrderRepository()
                    .findAll()
                    .stream()
                    .filter(order -> !order.isWithConfiguration())
                    .map(orderDtoMapper::toInStockCarOrderDto)
                    .toList();

            return new CheckInStockCarOrderList.Response(orders);
        }

        Optional<UUID> optionalClientId = findClientIdBySubAndIssuer(request.currentUser());
        if (optionalClientId.isEmpty()) {
            return new CheckInStockCarOrderList.Response(List.of());
        }
        UUID currentClientId = optionalClientId.get();

        var orders = persistenceContext.getOrderRepository()
                .findAll()
                .stream()
                .filter(order -> !order.isWithConfiguration())
                .filter(order -> order.getClientID().equals(currentClientId))
                .map(orderDtoMapper::toInStockCarOrderDto)
                .toList();

        return new CheckInStockCarOrderList.Response(orders);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CheckConfiguredCarOrderList.Response checkConfiguredCarOrderList(CheckConfiguredCarOrderList.Request request) {
        if (request == null) {
            throw new DomainValidationException("Check configured car order list request must not be null.");
        }
        validateCurrentUserForRead(request.currentUser());

        if (isPrivilegedForOrderRead(request.currentUser())) {
            var orders = persistenceContext.getOrderRepository()
                    .findAll()
                    .stream()
                    .filter(Order::isWithConfiguration)
                    .map(orderDtoMapper::toConfiguredCarOrderDto)
                    .toList();

            return new CheckConfiguredCarOrderList.Response(orders);
        }

        Optional<UUID> optionalClientId = findClientIdBySubAndIssuer(request.currentUser());
        if (optionalClientId.isEmpty()) {
            return new CheckConfiguredCarOrderList.Response(List.of());
        }
        UUID currentClientId = optionalClientId.get();

        var orders = persistenceContext.getOrderRepository()
                .findAll()
                .stream()
                .filter(Order::isWithConfiguration)
                .filter(order -> order.getClientID().equals(currentClientId))
                .map(orderDtoMapper::toConfiguredCarOrderDto)
                .toList();

        return new CheckConfiguredCarOrderList.Response(orders);
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional
    public UpdateOrder.Response updateOrder(UpdateOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Update order request must not be null.");
        }
        if (request.orderId() == null) {
            throw new DomainValidationException("Order id must not be null.");
        }
        if (request.change() == null) {
            throw new DomainValidationException("Order change must not be null.");
        }

        Order order = findOrderById(request.orderId());
        UpdateOrder.Change change = request.change();

        if (change instanceof UpdateOrder.AssignCar assignCar) {
            if (assignCar.carId() == null) {
                throw new DomainValidationException("Car id must not be null.");
            }
            if (!order.isWithConfiguration()) {
                throw new DomainValidationException(
                        "Assigning a car is only allowed for configured orders."
                );
            }

            Car currentCar = order.getCar();
            if (currentCar != null && currentCar.getId().equals(assignCar.carId())) {
                return new UpdateOrder.Response(order.getId());
            }

            Car newCar = findCarById(assignCar.carId());
            if (newCar.getStatus() != CarStockStatus.AVAILABLE) {
                throw new DomainValidationException(
                        "Car \"%s\" is not available for assignment."
                                .formatted(newCar.getId())
                );
            }

            order.assignCar(newCar);
            persistenceContext.getOrderRepository().save(order);

            return new UpdateOrder.Response(order.getId());
        }

        orderChangeApplier.applyChange(order, change);

        persistenceContext.getOrderRepository().save(order);

        if (change instanceof UpdateOrder.MarkPaid) {
            persistOrderSentForApprovalEvent(order);
        }

        return new UpdateOrder.Response(order.getId());
    }

    @Override
    @PreAuthorize("#request != null and @orderSecurity.canCancelOrder(#request.orderId, authentication)")
    public DeleteOrder.Response deleteOrder(DeleteOrder.Request request) {
        if (request == null) {
            throw new DomainValidationException("Delete order request must not be null.");
        }
        if (request.orderId() == null) {
            throw new DomainValidationException("Order id must not be null.");
        }

        Order order = findOrderById(request.orderId());

        order.markCancelled();

        persistenceContext.getOrderRepository().save(order);
        return new DeleteOrder.Response(order.getId());
    }

    private Client findClientById(UUID clientId) {
        return persistenceContext.getClientRepository()
                .findById(clientId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Client \"%s\" not found".formatted(clientId)
                        )
                );
    }

    private UUID findOrCreateClientBySubAndIssuer(CurrentUser currentUser) {
        Optional<UUID> optionalClientId = findClientIdBySubAndIssuer(currentUser);

        if (optionalClientId.isPresent()) {
            return optionalClientId.get();
        }

        Client createdClient = persistenceContext.getClientRepository().save(
                new Client(
                        UUID.randomUUID(),
                        resolveClientName(currentUser),
                        resolveClientSurname(currentUser)
                )
        );

        persistenceContext.getJwtSubRepository().bindClient(
                currentUser.issuer(),
                currentUser.subject(),
                createdClient.getId()
        );

        return createdClient.getId();
    }

    private Optional<UUID> findClientIdBySubAndIssuer(CurrentUser currentUser) {
        return persistenceContext
                .getJwtSubRepository()
                .findClientIdByIssuerAndSubject(currentUser.issuer(), currentUser.subject());
    }

    private void validateCurrentUserForRead(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new DomainValidationException("Current user must not be null.");
        }
        if (currentUser.issuer() == null || currentUser.issuer().isBlank()) {
            throw new DomainValidationException("Current user issuer must not be blank.");
        }
        if (currentUser.subject() == null || currentUser.subject().isBlank()) {
            throw new DomainValidationException("Current user subject must not be blank.");
        }
    }

    private void validateCurrentUserForPlacement(CurrentUser currentUser) {
        validateCurrentUserForRead(currentUser);
    }

    private String  resolveClientName(CurrentUser currentUser) {
        if (currentUser.name() != null && !currentUser.name().isBlank()) {
            return currentUser.name().trim();
        }
        return "Name-" + currentUser.subject();
    }

    private String resolveClientSurname(CurrentUser currentUser) {
        if (currentUser.surname() != null && !currentUser.surname().isBlank()) {
            return currentUser.surname().trim();
        }
        return "Surname";
    }

    private Manager resolveManagerForPlacement() {
        List<Manager> managers = persistenceContext.getManagerRepository().findAll();
        if (managers.isEmpty()) {
            throw new DomainValidationException("No managers available for order assignment.");
        }

        return managers.getFirst();
    }

    private boolean isPrivilegedForOrderRead(CurrentUser currentUser) {
        return hasRole(currentUser, "MANAGER") || hasRole(currentUser, "ADMIN");
    }

    private boolean hasRole(CurrentUser currentUser, String expectedRole) {
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        String normalizedExpectedRole = normalizeRole(expectedRole);
        for (String role : roles) {
            if (normalizedExpectedRole.equals(normalizeRole(role))) {
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

    private Order findOrderById(UUID orderId) {
        return persistenceContext.getOrderRepository()
                .findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Order \"%s\" not found".formatted(orderId)
                        )
                );
    }

    private Car findCarById(UUID carId) {
        return storageCatalogGateway.getInStockCar(carId);
    }

    private CarModel findCarModelById(UUID modelId) {
        return storageCatalogGateway.getCarModel(modelId);
    }

    private List<SlotVariant> resolveSpareParts(List<SlotRefDto> slotRefs, CarModel currentModel) {
        return slotRefs.stream()
                .map(SlotRefDto::id)
                .map(slotVariantId -> storageCatalogGateway.getSparePartAsSlotVariant(slotVariantId, currentModel))
                .toList();
    }

    private void validateDuplicateSelectedSlots(List<SlotVariant> selectedSlots) {
        Set<SlotType> selectedSlotTypes = new HashSet<>();

        for (SlotVariant selectedSlot : selectedSlots) {
            if (!selectedSlotTypes.add(selectedSlot.getSlot())) {
                throw new DomainValidationException(
                        "Duplicate selected slot type \"%s\" is not allowed."
                                .formatted(selectedSlot.getSlot())
                );
            }
        }
    }

    private CarConfiguration createConfiguration(UUID modelId, List<SlotRefDto> selectedSlots) {
        if (modelId == null) {
            throw new DomainValidationException("Car model id must not be null.");
        }
        if (selectedSlots == null) {
            throw new DomainValidationException("Selected slots must not be null.");
        }

        CarModel carModel = findCarModelById(modelId);
        List<SlotVariant> resolvedSlots = resolveSpareParts(selectedSlots, carModel);
        validateDuplicateSelectedSlots(resolvedSlots);

        CarConfiguration configuration = new CarConfiguration(carModel);
        for (SlotVariant selectedSlot : resolvedSlots) {
            configuration.setSlot(selectedSlot);
        }
        return configuration;
    }

    private void persistOrderSentForApprovalEvent(Order order) {
        String traceId = resolveTraceId();
        OrderSentForApprovalEvent event = buildOrderSentForApprovalEvent(order, traceId);

        persistenceContext.getOutboxEventRepository().save(
                new NewOutboxEvent(
                        UUID.randomUUID(),
                        order.getId(),
                        "ORDER",
                        OrderSentForApprovalEvent.class.getSimpleName(),
                        RabbitContracts.ORDERS_EXCHANGE,
                        RabbitContracts.ORDER_SENT_FOR_APPROVAL_ROUTING_KEY,
                        serializeEvent(event),
                        traceId
                )
        );
    }

    private OrderSentForApprovalEvent buildOrderSentForApprovalEvent(Order order, String traceId) {
        SourceOrderType sourceOrderType = order.isWithConfiguration()
                ? SourceOrderType.CONFIGURED
                : SourceOrderType.IN_STOCK;

        UUID carId = order.getCar() == null ? null : order.getCar().getId();
        UUID carModelId = order.getRequestedConfiguration().getModel().getId();

        Set<UUID> slotVariantIds = order.getRequestedConfiguration()
                .getSelectedSlots()
                .values()
                .stream()
                .map(slot -> slot == null ? null : slot.getId())
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return new OrderSentForApprovalEvent(
                order.getId(),
                traceId,
                sourceOrderType,
                carId,
                carModelId,
                slotVariantIds,
                Instant.now()
        );
    }

    private String serializeEvent(OrderSentForApprovalEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox event payload.", ex);
        }
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
