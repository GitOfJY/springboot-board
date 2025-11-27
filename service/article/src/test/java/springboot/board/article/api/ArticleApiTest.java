package springboot.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import springboot.board.article.service.response.ArticlePageResponse;
import springboot.board.article.service.response.ArticleResponse;

import java.util.List;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "my content", 1L, 1L
        ));
        System.out.println("response: " + response);
        // response: ArticleResponse(articleId=251512420604350464, title=hi, content=my content, boardId=1, writerId=1, createdAt=null, modifiedAt=null)
    }

    @Test
    void readTest() {
        ArticleResponse response = read(251512420604350464L);
        System.out.println("response: " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(1L);
        ArticleResponse response = read(251512420604350464L);
        System.out.println("response: " + response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hi 2", "mu content 2"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 251512420604350464L)
                .retrieve();
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Getter
    @AllArgsConstructor
    public class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    public class ArticleUpdateRequest {
        private String title;
        private String content;
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=1")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount(): " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("article: " + article);
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> articleResponses = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });
        System.out.println("firstPage");
        for (ArticleResponse article : articleResponses) {
            System.out.println("article: " + article.getArticleId());
        }

        Long lastArticleId = articleResponses.get(articleResponses.size() - 1).getArticleId();
        List<ArticleResponse> articleResponses2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });
        System.out.println("secondPage");
        for (ArticleResponse article : articleResponses2) {
            System.out.println("article: " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest1() {
        try {
            List<ArticleResponse> articleResponses = restClient.get()
                    .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=30")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        System.out.println("Status: " + response.getStatusCode());
                        System.out.println("Body: " + new String(response.getBody().readAllBytes()));
                        throw new RuntimeException("API Error");
                    })
                    .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "my content", 1L, 2L));
        Long count1 = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count1 : " + count1);

        restClient.delete()
                .uri("/v1/articles/{articleId}", response.getArticleId())
                .retrieve();

        Long count2 = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count2 : " + count2);
    }
}
