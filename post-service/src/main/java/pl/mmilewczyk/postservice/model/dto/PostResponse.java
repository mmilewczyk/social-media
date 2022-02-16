package pl.mmilewczyk.postservice.model.dto;


import pl.mmilewczyk.postservice.model.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        String title,
        String authorUsername,
        LocalDateTime createdAt,
        String body,
        Long likes,
        List<Comment> comments) {
}
