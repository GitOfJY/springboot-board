package springboot.board.hotarticle.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventPayload;
import springboot.board.common.event.EventType;
import springboot.board.hotarticle.service.HotArticleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    @KafkaListener(topics = {
            EventType.Topic.SPRINGBOOT_BOARD_ARTICLE,
            EventType.Topic.SPRINGBOOT_BOARD_COMMENT,
            EventType.Topic.SPRINGBOOT_BOARD_LIKE,
            EventType.Topic.SPRINGBOOT_BOARD_VIEW
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer] message: {}]", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            hotArticleService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
