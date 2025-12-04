package springboot.board.articleread.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import springboot.board.articleread.repository.ArticleIdListRepository;
import springboot.board.articleread.repository.ArticleQueryModel;
import springboot.board.articleread.repository.ArticleQueryModelRepository;
import springboot.board.articleread.repository.BoardArticleCountRepository;
import springboot.board.common.event.Event;
import springboot.board.common.event.EventType;
import springboot.board.common.event.payload.ArticleCreatedEventPayload;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getArticleId());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}
