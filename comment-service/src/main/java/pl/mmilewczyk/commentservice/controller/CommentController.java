package pl.mmilewczyk.commentservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.commentservice.model.dto.CommentRequest;
import pl.mmilewczyk.commentservice.model.dto.CommentResponse;
import pl.mmilewczyk.commentservice.service.CommentService;

import java.util.List;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(path = "api/v1/comments")
public record CommentController(CommentService commentService) {

    @GetMapping("/{commentId}")
    ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") Long id) {
        return status(HttpStatus.OK).body(commentService.getCommentResponseById(id));
    }

    @PostMapping("/{postId}")
    ResponseEntity<CommentResponse> addNewCommentToThePost(
            @RequestBody CommentRequest commentRequest,
            @PathVariable("postId") Long id) {
        return status(HttpStatus.CREATED).body(commentService.createNewComment(commentRequest, id));
    }

    @GetMapping
    ResponseEntity<Page<CommentResponse>> getAllCommentsOfThePost(@RequestParam("postId") Long id) {
        return status(HttpStatus.OK).body(commentService.getAllCommentsOfThePost(id));
    }

    @GetMapping("/technical/list")
    List<CommentResponse> technicalGetAllCommentsOfThePost(@RequestParam("postId") Long id) {
        return commentService.getAllCommentsOfThePost(id).getContent();
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCommentById(@PathVariable("commentId") Long commentId) {
        commentService.deleteCommentById(commentId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> editCommentById(@PathVariable("id") Long commentId,
                                                           @RequestBody CommentRequest commentRequest) {
        return status(HttpStatus.CREATED).body(commentService.editCommentById(commentId, commentRequest));
    }
}
