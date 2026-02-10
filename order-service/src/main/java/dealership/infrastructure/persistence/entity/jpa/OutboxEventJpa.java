package dealership.infrastructure.persistence.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpa extends BaseJpa {

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "exchange_name", nullable = false, length = 128)
    private String exchangeName;

    @Column(name = "routing_key", nullable = false, length = 128)
    private String routingKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "trace_id", nullable = false, length = 128)
    private String traceId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public OutboxEventJpa(
            UUID id,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String exchangeName,
            String routingKey,
            String payload,
            String traceId
    ) {
        super(id);
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
        this.traceId = traceId;
        this.status = "NEW";
        this.attempts = 0;
    }
}
