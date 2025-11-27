package springboot.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import springboot.board.comment.service.response.CommentPageResponse;
import springboot.board.comment.service.response.CommentResponse;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\t\tcommentId=%s".formatted(response3.getCommentId()));
        // commentId=252238814154854400
        //	 commentId=252238814641393664
        //		commentId=252238814784000000

        System.out.println("commentPath=%s".formatted(response1.getPath()));
        System.out.println("\tcommentPath=%s".formatted(response2.getPath()));
        System.out.println("\t\tcommentPath=%s".formatted(response3.getPath()));
        // commentPath=00000
        //	 commentPath=0000000000
        //		commentPath=000000000000000
    }

    CommentResponse createComment(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 252238814154854400L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
        // response = CommentResponse(commentId=252238814154854400, content=my comment1, parentCommentId=null, path=00000, articleId=1, writerId=1, deleted=false, createdAt=2025-11-27T10:07)
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 252238814784000000L)
                .retrieve();
    }

    @AllArgsConstructor
    @Getter
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {});
        System.out.println("first Page");
        for (CommentResponse response : response1) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }

        String lastPath = response1.getLast().getPath();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {});
        System.out.println("second Page");
        for (CommentResponse response : response2) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }
    }

    @Test
    void countTest() {
        CommentResponse commentResponse = createComment(new CommentCreateRequestV2(2L, "content", null, 1L));

        Long count1 = restClient.get()
                .uri("/v2/comments/articles/{articleId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count1 = " + count1);

        restClient.delete()
                .uri("/v2/comments/{commentId}", commentResponse.getCommentId())
                .retrieve();

        Long count2 = restClient.get()
                .uri("/v2/comments/articles/{articleId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count2 = " + count2);
    }

}
