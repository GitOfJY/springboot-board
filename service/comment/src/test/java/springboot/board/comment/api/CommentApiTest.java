package springboot.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import springboot.board.comment.entity.Comment;
import springboot.board.comment.service.response.CommentPageResponse;
import springboot.board.comment.service.response.CommentResponse;

import java.util.List;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));
        // commentId=251578466570665984
        // commentId=251578468307107840
        // commentId=251578468474880000
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 251578468474880000L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v1/comments/{commentId}", 251578468474880000L)
                .retrieve();
    }


    @AllArgsConstructor
    @Getter
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /*
    [page=1]
    response.getCommentCount() = 101
    comment.getCommentId() = 251580678521499648
    	comment.getCommentId() = 251580678714437636
    comment.getCommentId() = 251580678521499649
    	comment.getCommentId() = 251580678714437635
    comment.getCommentId() = 251580678521499650
    	comment.getCommentId() = 251580678714437644
    comment.getCommentId() = 251580678521499651
    	comment.getCommentId() = 251580678714437632
    comment.getCommentId() = 251580678521499652
    	comment.getCommentId() = 251580678714437633
    * */

    /*
    [first page]
    comment.getCommentId() = 251580678521499648
        comment.getCommentId() = 251580678714437636
    comment.getCommentId() = 251580678521499649
        comment.getCommentId() = 251580678714437635
    comment.getCommentId() = 251580678521499650
    [second page]
        comment.getCommentId() = 251580678714437644
    comment.getCommentId() = 251580678521499651
        comment.getCommentId() = 251580678714437632
    comment.getCommentId() = 251580678521499652
        comment.getCommentId() = 251580678714437633
    * */

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("first page");
        for (CommentResponse comment : response1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastCommentId = response1.getLast().getCommentId();
        Long lastParentCommentId = response1.getLast().getParentCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastCommentId, lastParentCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("second page");
        for (CommentResponse comment : response2) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }
}
