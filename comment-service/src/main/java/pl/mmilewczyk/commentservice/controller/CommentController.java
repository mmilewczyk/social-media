package pl.mmilewczyk.commentservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.commentservice.model.dto.CommentRequest;
import pl.mmilewczyk.commentservice.service.CommentService;

@RestController
@RequestMapping(path = "api/v1/comments")
public record CommentController(CommentService commentService) {

    @GetMapping("/{commentId}")
    ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getCommentById(id));
    }

    @PostMapping("/{postId}")
    ResponseEntity<CommentResponse> addNewCommentToThePost(
            @RequestBody CommentRequest commentRequest,
            @PathVariable("postId") Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createNewComment(commentRequest, id));
    }

    @GetMapping
    ResponseEntity<Page<CommentResponse>> getAllCommentsOfThePost(@RequestParam("postId") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getAllCommentsOfThePost(id));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCommentById(@PathVariable("commentId") Long commentId) {
        commentService.deleteCommentById(commentId);
    }
}
