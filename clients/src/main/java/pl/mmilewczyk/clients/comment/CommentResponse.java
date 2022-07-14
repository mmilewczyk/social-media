package pl.mmilewczyk.clients.comment;

import java.time.LocalDateTime;

public record CommentResponse(
        String authorUsername,
        LocalDateTime createdAt,
        Boolean wasEdited,
        String body,
        Long likes) {
}
