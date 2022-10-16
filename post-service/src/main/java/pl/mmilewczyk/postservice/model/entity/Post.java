package pl.mmilewczyk.postservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.dto.PostResponseLite;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @SequenceGenerator(name = "post_id_sequence", sequenceName = "post_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "post_id_sequence")
    private Long postId;
    private String title;
    private Long authorId;
    private LocalDateTime createdAt;

    //@Lob
    private String body;

    private Long likes;

    @ElementCollection
    private List<Long> commentsIds;

    public boolean isComplete() {
        return title != null && authorId != null && body != null;
    }

    public PostResponse mapToPostResponse(UserResponseWithId author, List<CommentResponse> commentResponses) {
        return new PostResponse(
                this.getPostId(),
                this.getTitle(),
                authorId,
                author.username(),
                formatDate(getCreatedAt()),
                this.getBody(),
                this.getLikes(),
                commentResponses);
    }

    public PostResponseLite mapToPostResponseLite(UserResponseWithId author, List<CommentResponse> commentResponses) {
        commentsIds = commentResponses.stream().map(CommentResponse::commentId).toList();
        return new PostResponseLite(
                this.getPostId(),
                this.getTitle(),
                authorId,
                author.username(),
                formatDate(getCreatedAt()),
                this.getBody(),
                this.getLikes(),
                commentResponses.size());
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = ofPattern("dd/MM/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }
}
