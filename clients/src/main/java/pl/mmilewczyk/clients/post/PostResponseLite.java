package pl.mmilewczyk.clients.post;

public record PostResponseLite(String title,
                               Long authorId,
                               String authorUsername,
                               String createdAt,
                               String body,
                               Long likes,
                               long comments) {
}
