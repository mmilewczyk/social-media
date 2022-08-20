package pl.mmilewczyk.postservice.model.dto;

import java.time.LocalDateTime;

public record PostResponseLite(Long postId,
                               String title,
                               String authorUsername,
                               LocalDateTime createdAt,
                               String body,
                               Long likes) {
}
