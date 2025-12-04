package springboot.board.articleread.service.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ArticleReadPageResponse {
    private List<ArticleReadResponse> articles;
    private Long articleCount;

    public static ArticleReadPageResponse of(List<ArticleReadResponse> articles, Long articleCount) {
        ArticleReadPageResponse articleReadPageResponse = new ArticleReadPageResponse();
        articleReadPageResponse.articles = articles;
        articleReadPageResponse.articleCount = articleCount;
        return articleReadPageResponse;
    }
}
