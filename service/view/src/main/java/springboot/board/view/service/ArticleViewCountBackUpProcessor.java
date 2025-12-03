package springboot.board.view.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import springboot.board.common.event.EventType;
import springboot.board.common.event.payload.ArticleViewEventPayload;
import springboot.board.common.outboxmessagerelay.OutboxEventPublisher;
import springboot.board.view.entity.ArticleViewCount;
import springboot.board.view.repository.ArticleViewCountBackUpRepository;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if (result == 0) {
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> {},
                            () -> articleViewCountBackUpRepository.save(
                                    ArticleViewCount.init(articleId, viewCount)
                            ));
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}
