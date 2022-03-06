package pl.mmilewczyk.commentservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @SequenceGenerator(name = "comment_id_sequence", sequenceName = "comment_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_sequence")
    private Long commentId;

    private Long postId;

    private Long authorId;
    private LocalDateTime createdAt;

    @Lob
    private String body;

    private Long likes;

    public CommentResponse mapToCommentResponse(UserResponseWithId user) {
        return new CommentResponse(user.username(), this.createdAt, this.body, this.likes);
    }

    public boolean isComplete() {
        return postId != null && authorId != null && body != null;
    }
}
