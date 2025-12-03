package springboot.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventPayload;
import springboot.board.common.event.EventType;
import springboot.board.common.snowflake.Snowflake;

@ComponentScan
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(EventType type, EventPayload payload, Long shardKey) {
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(),
                type,
                Event.of(
                        eventIdSnowflake.nextId(),
                        type,
                        payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
