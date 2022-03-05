package pl.mmilewczyk.postservice.model.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        String authorUsername,
        LocalDateTime createdAt,
        String body,
        Long likes) {
}
