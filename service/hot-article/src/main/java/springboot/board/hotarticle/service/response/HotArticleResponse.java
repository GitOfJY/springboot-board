package springboot.board.hotarticle.service.response;

import lombok.Getter;
import springboot.board.hotarticle.client.ArticleClient;

import java.time.LocalDateTime;

@Getter
public class HotArticleResponse {
    private Long articleId;
    private String title;
    private LocalDateTime createdAt;

    public static HotArticleResponse from(ArticleClient.ArticleResponse articleResponse) {
        HotArticleResponse response = new HotArticleResponse();
        response.articleId = articleResponse.getArticleId();
        response.title = articleResponse.getTitle();
        response.createdAt = articleResponse.getCreatedAt();
        return response;
    }
}
