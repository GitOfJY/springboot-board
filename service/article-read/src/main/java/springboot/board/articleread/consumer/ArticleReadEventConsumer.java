package springboot.board.articleread.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import springboot.board.articleread.service.ArticleReadService;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventPayload;
import springboot.board.common.event.EventType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    @KafkaListener(topics = {
            EventType.Topic.SPRINGBOOT_BOARD_ARTICLE,
            EventType.Topic.SPRINGBOOT_BOARD_COMMENT,
            EventType.Topic.SPRINGBOOT_BOARD_LIKE
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[ArticleReadEventConsumer] message: {}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            articleReadService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
