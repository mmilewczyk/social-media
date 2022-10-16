package pl.mmilewczyk.clients.post;

import pl.mmilewczyk.clients.comment.CommentResponse;

import java.util.List;

public record PostResponse(
        Long postId,
        String title,
        Long authorId,
        String authorUsername,
        String createdAt,
        String body,
        Long likes,
        List<CommentResponse> comments) {
}
