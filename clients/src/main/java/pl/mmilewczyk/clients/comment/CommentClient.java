package pl.mmilewczyk.clients.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("COMMENT-SERVICE")
public interface CommentClient {

    @GetMapping("api/v1/comments/{commentId}")
    ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") Long id);

    @GetMapping("api/v1/comments/technical/list")
    List<CommentResponse> technicalGetAllCommentsOfThePost(@RequestParam("postId") Long id);

    @DeleteMapping("api/v1/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCommentById(@PathVariable("commentId") Long commentId);
}
