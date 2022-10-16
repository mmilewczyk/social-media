package pl.mmilewczyk.postservice.model.dto;

public record PostResponseLite(Long postId,
                               String title,
                               Long authorId,
                               String authorUsername,
                               String createdAt,
                               String body,
                               Long likes,
                               long comments) {
}
