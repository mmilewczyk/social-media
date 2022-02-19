package pl.mmilewczyk.postservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @SequenceGenerator(name = "post_id_sequence", sequenceName = "post_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_id_sequence")
    private Long postId;
    private String title;
    private Long authorId;
    private LocalDateTime createdAt;

    @Lob
    private String body;

    private Long likes;

    @OneToMany
    private List<Comment> comments;

    public boolean isComplete() {
        return title != null && authorId != null && body != null;
    }
}
