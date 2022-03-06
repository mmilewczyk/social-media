package pl.mmilewczyk.clients.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("COMMENT")
public interface CommentClient {

    @GetMapping("api/v1/comments/{commentId}")
    ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") Long id);
}
