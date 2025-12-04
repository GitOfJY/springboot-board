package springboot.board.articleread.service.event.handler;

import springboot.board.common.event.Event;
import springboot.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
