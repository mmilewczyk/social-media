package pl.mmilewczyk.clients.comment;

public record CommentResponse(
        Long commentId,
        Long authorId,
        String authorUsername,
        String createdAt,
        Boolean wasEdited,
        String body,
        Long likes) {
}
