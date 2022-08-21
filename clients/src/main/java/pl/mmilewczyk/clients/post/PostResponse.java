package pl.mmilewczyk.clients.post;

import pl.mmilewczyk.clients.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        String title,
        String authorUsername,
        LocalDateTime createdAt,
        String body,
        Long likes,
        List<CommentResponse> comments) {
}
