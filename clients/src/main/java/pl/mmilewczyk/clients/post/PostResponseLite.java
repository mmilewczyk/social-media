package pl.mmilewczyk.clients.post;

import java.time.LocalDateTime;

public record PostResponseLite(String title,
                               String authorUsername,
                               LocalDateTime createdAt,
                               String body,
                               Long likes) {
}
