package pl.mmilewczyk.commentservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.commentservice.model.dto.CommentResponse;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @SequenceGenerator(name = "comment_id_sequence", sequenceName = "comment_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "comment_id_sequence")
    private Long commentId;

    private Long postId;

    private Long authorId;
    private LocalDateTime createdAt;

    @Lob
    private String body;

    private Long likes;

    private boolean wasEdited;

    public CommentResponse mapToCommentResponse(UserResponseWithId user) {
        return new CommentResponse(this.commentId, this.authorId, user.username(), formatDate(createdAt), this.wasEdited, this.body, this.likes);
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = ofPattern("dd/MM/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public boolean isComplete() {
        return postId != null && authorId != null && body != null;
    }
}
