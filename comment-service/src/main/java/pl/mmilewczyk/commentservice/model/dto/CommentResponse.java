package pl.mmilewczyk.commentservice.model.dto;


public record CommentResponse(
        Long commentId,
        Long authorId,
        String authorUsername,
        String createdAt,
        Boolean wasEdited,
        String body,
        Long likes) {
}
