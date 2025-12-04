package springboot.board.articleread.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import springboot.board.articleread.repository.ArticleQueryModelRepository;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventType;
import springboot.board.common.event.payload.ArticleUpdatedEventPayload;
import springboot.board.common.event.payload.CommentCreatedEventPayload;

@Component
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<CommentCreatedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<CommentCreatedEventPayload> event) {
        return EventType.COMMENT_CREATED == event.getType();
    }
}
