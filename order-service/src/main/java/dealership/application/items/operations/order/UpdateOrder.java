package dealership.application.items.operations.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

public final class UpdateOrder {

    private UpdateOrder() {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AssignCar.class, name = "assignCar"),
            @JsonSubTypes.Type(value = MarkApproved.class, name = "markApproved"),
            @JsonSubTypes.Type(value = MarkWaitingForPayment.class, name = "markWaitingForPayment"),
            @JsonSubTypes.Type(value = MarkPaid.class, name = "markPaid"),
            @JsonSubTypes.Type(value = MarkWaitingForDelivery.class, name = "markWaitingForDelivery"),
            @JsonSubTypes.Type(value = MarkReadyForPickup.class, name = "markReadyForPickup"),
            @JsonSubTypes.Type(value = MarkCompleted.class, name = "markCompleted"),
            @JsonSubTypes.Type(value = MarkCancelled.class, name = "markCancelled")
    })
    public sealed interface Change permits
            AssignCar,
            MarkApproved,
            MarkWaitingForPayment,
            MarkPaid,
            MarkWaitingForDelivery,
            MarkReadyForPickup,
            MarkCompleted,
            MarkCancelled {
    }

    public record AssignCar(UUID carId) implements Change {
    }

    public record MarkApproved() implements Change {
    }

    public record MarkWaitingForPayment() implements Change {
    }

    public record MarkPaid() implements Change {
    }

    public record MarkWaitingForDelivery() implements Change {
    }

    public record MarkReadyForPickup() implements Change {
    }

    public record MarkCompleted() implements Change {
    }

    public record MarkCancelled() implements Change {
    }

    public record Request(UUID orderId, Change change) {
    }

    public record Response(UUID orderId) {
    }
}
