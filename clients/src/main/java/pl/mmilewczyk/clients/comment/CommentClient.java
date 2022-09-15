package pl.mmilewczyk.clients.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@FeignClient("COMMENT-SERVICE")
public interface CommentClient {

    String BASE_URL = "api/v1/comments/";

    @GetMapping(BASE_URL + "{commentId}")
    ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") Long id);

    @GetMapping(BASE_URL + "technical/list")
    List<CommentResponse> technicalGetAllCommentsOfThePost(@RequestParam("postId") Long id);

    @DeleteMapping(BASE_URL + "{commentId}")
    @ResponseStatus(NO_CONTENT)
    void deleteCommentById(@PathVariable("commentId") Long commentId);
}
