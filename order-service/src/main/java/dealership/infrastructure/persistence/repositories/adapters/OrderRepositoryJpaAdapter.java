package dealership.infrastructure.persistence.repositories.adapters;

import dealership.application.abstractions.integration.StorageCatalogGateway;
import dealership.application.abstractions.persistence.OrderRepository;
import dealership.domain.configuration.CarConfiguration;
import dealership.domain.entities.cars.Car;
import dealership.domain.entities.slots.SlotType;
import dealership.domain.entities.slots.SlotVariant;
import dealership.domain.orders.Order;
import dealership.infrastructure.persistence.entity.jpa.CarConfigurationJpa;
import dealership.infrastructure.persistence.entity.jpa.CarConfigurationSelectedSlotJpa;
import dealership.infrastructure.persistence.entity.jpa.OrderJpa;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSelectedSlotSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.CarConfigurationSpringDataRepository;
import dealership.infrastructure.persistence.repositories.jpa.OrderSpringDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class OrderRepositoryJpaAdapter implements OrderRepository {

    private final OrderSpringDataRepository orderSpringDataRepository;
    private final CarConfigurationSpringDataRepository carConfigurationSpringDataRepository;
    private final CarConfigurationSelectedSlotSpringDataRepository carConfigurationSelectedSlotSpringDataRepository;
    private final StorageCatalogGateway storageCatalogGateway;

    @Override
    @Transactional
    public Order save(Order entity) {
        UUID orderId = requireId(entity.getId(), "Order id must not be null.");

        Optional<OrderJpa> existingOrder = orderSpringDataRepository.findById(orderId);
        UUID requestedConfigurationId = existingOrder
                .map(OrderJpa::getRequestedConfigurationId)
                .orElse(UUID.randomUUID());

        // Requested configuration is immutable after order creation in current domain use-cases.
        // Rewriting configuration rows on every status transition is redundant and may trigger
        // integrity conflicts on selected slot rows in integration flows.
        if (existingOrder.isEmpty()) {
            saveConfiguration(requestedConfigurationId, entity.getRequestedConfiguration());
        }

        UUID carId = entity.getCar() == null ? null : requireId(entity.getCar().getId(), "Assigned car id must not be null.");

        UUID clientId = requireId(entity.getClientID(), "Order client id must not be null.");
        UUID managerId = requireId(entity.getManagerID(), "Order manager id must not be null.");

        String status = entity.getStatus();
        if (status == null || status.isBlank()) {
            throw new IllegalStateException("Order status must not be blank.");
        }

        if (existingOrder.isPresent()) {
            OrderJpa orderJpa = existingOrder.get();
            orderJpa.setWithConfiguration(entity.isWithConfiguration());
            orderJpa.setClientId(clientId);
            orderJpa.setManagerId(managerId);
            orderJpa.setRequestedConfigurationId(requestedConfigurationId);
            orderJpa.setCarId(carId);
            orderJpa.setStatus(status);
            orderSpringDataRepository.save(orderJpa);
        } else {
            orderSpringDataRepository.save(new OrderJpa(
                    orderId,
                    entity.isWithConfiguration(),
                    clientId,
                    managerId,
                    requestedConfigurationId,
                    carId,
                    status
            ));
        }

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return orderSpringDataRepository.findById(id)
                .map(this::assembleOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderSpringDataRepository.findAll().stream()
                .map(this::assembleOrder)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        orderSpringDataRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return orderSpringDataRepository.existsById(id);
    }

    private Order assembleOrder(OrderJpa orderJpa) {
        CarConfiguration requestedConfiguration;
        requestedConfiguration = assembleConfiguration(orderJpa.getRequestedConfigurationId());

        Car assignedCar = null;
        if (orderJpa.getCarId() != null) {
            assignedCar = new Car(orderJpa.getCarId(), requestedConfiguration);
        }

        Order order = new Order(
                orderJpa.getId(),
                orderJpa.isWithConfiguration(),
                orderJpa.getClientId(),
                orderJpa.getManagerId(),
                requestedConfiguration,
                assignedCar
        );

        applyPersistedStatus(order, orderJpa.getStatus());
        return order;
    }

    private void saveConfiguration(UUID configurationId, CarConfiguration configuration) {
        UUID modelId = requireId(configuration.getModel().getId(), "Car configuration model id must not be null.");
        carConfigurationSpringDataRepository.save(new CarConfigurationJpa(configurationId, modelId));

        carConfigurationSelectedSlotSpringDataRepository.deleteAllByConfigurationId(configurationId);

        List<CarConfigurationSelectedSlotJpa> selectedSlots = configuration.getSelectedSlots().entrySet().stream()
                .map(entry -> {
                    SlotType slotType = entry.getKey();
                    if (slotType == null) {
                        throw new IllegalStateException("Car configuration slot type must not be null.");
                    }

                    SlotVariant slotVariant = entry.getValue();
                    if (slotVariant == null) {
                        throw new IllegalStateException(
                                "Car configuration slot variant must not be null for slot type " + slotType
                        );
                    }

                    UUID slotVariantId = requireId(
                            slotVariant.getId(),
                            "Car configuration slot variant id must not be null for slot type " + slotType
                    );

                    return new CarConfigurationSelectedSlotJpa(configurationId, slotType.name(), slotVariantId);
                })
                .toList();

        carConfigurationSelectedSlotSpringDataRepository.saveAll(selectedSlots);
    }

    private CarConfiguration assembleConfiguration(UUID configurationId) {
        CarConfigurationJpa configurationJpa = carConfigurationSpringDataRepository.findById(configurationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Car configuration \"%s\" referenced by order was not found."
                                .formatted(configurationId)
                ));

        var carModel = storageCatalogGateway.getCarModel(configurationJpa.getModelId());

        CarConfiguration configuration = new CarConfiguration(carModel);
        List<CarConfigurationSelectedSlotJpa> selectedSlotRows =
                carConfigurationSelectedSlotSpringDataRepository.findAllByConfigurationId(configurationId);

        for (CarConfigurationSelectedSlotJpa selectedSlotRow : selectedSlotRows) {
            SlotType slotType = parseSlotType(selectedSlotRow.getSlotType());
            SlotVariant slotVariant = storageCatalogGateway.getSparePartAsSlotVariant(
                    selectedSlotRow.getSlotVariantId(),
                    carModel
            );

            if (slotVariant.getSlot() != slotType) {
                throw new IllegalStateException(
                        "Slot type mismatch in configuration \"%s\": row has \"%s\", variant has \"%s\"."
                                .formatted(configurationId, slotType, slotVariant.getSlot())
                );
            }

            if (carModel.getReplaceableSlots().contains(slotType)) {
                configuration.setSlot(slotVariant);
                continue;
            }

            SlotVariant baseVariant = carModel.getBaseSlots().get(slotType);
            if (baseVariant == null) {
                throw new IllegalStateException(
                        "Configuration \"%s\" contains non-base non-replaceable slot \"%s\"."
                                .formatted(configurationId, slotType)
                );
            }

            UUID baseVariantId = baseVariant.getId();
            UUID selectedVariantId = slotVariant.getId();
            if (baseVariantId == null || selectedVariantId == null || !baseVariantId.equals(selectedVariantId)) {
                throw new IllegalStateException(
                        "Configuration \"%s\" has illegal override for non-replaceable slot \"%s\"."
                                .formatted(configurationId, slotType)
                );
            }
        }

        return configuration;
    }

    private SlotType parseSlotType(String rawSlotType) {
        if (rawSlotType == null || rawSlotType.isBlank()) {
            throw new IllegalStateException("Slot type must not be blank in persistence data.");
        }
        try {
            return SlotType.valueOf(
                    rawSlotType.trim()
                            .toUpperCase()
                            .replace('-', '_')
                            .replace(' ', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported slot type in persistence data: " + rawSlotType, ex);
        }
    }

    private void applyPersistedStatus(Order order, String rawStatus) {
        String normalizedStatus = normalizeStatus(rawStatus);
        switch (normalizedStatus) {
            case "placed" -> {
                return;
            }
            case "approved by manager", "approved by warehouse" -> order.markApproved();
            case "waiting for payment" -> {
                order.markApproved();
                order.markWaitingForPayment();
            }
            case "paid" -> {
                order.markApproved();
                order.markWaitingForPayment();
                order.markPaid();
            }
            case "waiting for delivery" -> {
                order.markApproved();
                order.markWaitingForPayment();
                order.markPaid();
                order.markWaitingForDelivery();
            }
            case "ready for pickup" -> {
                order.markApproved();
                order.markWaitingForPayment();
                order.markPaid();
                if (order.isWithConfiguration()) {
                    order.markWaitingForDelivery();
                }
                order.markReadyForPickup();
            }
            case "completed" -> {
                order.markApproved();
                order.markWaitingForPayment();
                order.markPaid();
                if (order.isWithConfiguration()) {
                    order.markWaitingForDelivery();
                }
                order.markReadyForPickup();
                order.markCompleted();
            }
            case "cancelled" -> order.markCancelled();
            default -> throw new IllegalStateException("Unsupported order status in persistence data: " + rawStatus);
        }
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalStateException("Order status must not be blank in persistence data.");
        }
        return rawStatus.trim()
                .toLowerCase()
                .replace('_', ' ')
                .replace('-', ' ');
    }

    private UUID requireId(UUID id, String message) {
        if (id == null) {
            throw new IllegalStateException(message);
        }
        return id;
    }
}
