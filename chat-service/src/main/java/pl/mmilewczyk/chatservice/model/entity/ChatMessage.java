package pl.mmilewczyk.chatservice.model.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;
    private Long recipientId;
    private LocalDateTime sentAt;
    private String content;

    public ChatMessage(Long authorId, Long recipientId, String content, LocalDateTime sentAt) {
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.content = content;
        this.sentAt = sentAt;
    }
}
