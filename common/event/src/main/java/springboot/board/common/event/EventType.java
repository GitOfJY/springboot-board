package springboot.board.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.board.common.event.payload.*;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.SPRINGBOOT_BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.SPRINGBOOT_BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.SPRINGBOOT_BOARD_ARTICLE),

    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.SPRINGBOOT_BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.SPRINGBOOT_BOARD_COMMENT),

    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.SPRINGBOOT_BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.SPRINGBOOT_BOARD_LIKE),

    ARTICLE_VIEWED(ArticleViewEventPayload.class, Topic.SPRINGBOOT_BOARD_VIEW),;

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String SPRINGBOOT_BOARD_ARTICLE = "springboot-board-article";
        public static final String SPRINGBOOT_BOARD_COMMENT = "springboot-board-comment";
        public static final String SPRINGBOOT_BOARD_LIKE = "springboot-board-like";
        public static final String SPRINGBOOT_BOARD_VIEW = "springboot-board-view";
    }
}
