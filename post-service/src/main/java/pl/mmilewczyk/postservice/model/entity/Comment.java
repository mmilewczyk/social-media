package pl.mmilewczyk.postservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @SequenceGenerator(name = "comment_id_sequence", sequenceName = "comment_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_sequence")
    private Long commentId;
    private Long authorId;
    private LocalDateTime createdAt;

    @Lob
    private String body;

    private Long likes;
}
