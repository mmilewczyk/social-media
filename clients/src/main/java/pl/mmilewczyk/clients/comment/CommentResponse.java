package pl.mmilewczyk.clients.comment;

import java.time.LocalDateTime;

public record CommentResponse(
        String authorUsername,
        LocalDateTime createdAt,
        String body,
        Long likes) {
}
