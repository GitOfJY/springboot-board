package springboot.board.articleread.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import springboot.board.articleread.repository.ArticleQueryModel;
import springboot.board.articleread.repository.ArticleQueryModelRepository;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventType;
import springboot.board.common.event.payload.ArticleCreatedEventPayload;
import springboot.board.common.event.payload.ArticleUpdatedEventPayload;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleUpdatedEventHandler implements EventHandler<ArticleUpdatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUpdatedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUpdatedEventPayload> event) {
        return EventType.ARTICLE_UPDATED == event.getType();
    }
}
