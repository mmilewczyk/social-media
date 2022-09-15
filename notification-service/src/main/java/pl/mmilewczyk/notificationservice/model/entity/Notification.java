package pl.mmilewczyk.notificationservice.model.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Notification {

    @Id
    @SequenceGenerator(name = "notification_id_sequence", sequenceName = "notification_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "notification_id_sequence")
    private Long notificationId;
    private Long toUserId;
    private String toUserEmail;
    private String sender;
    private String message;
    private LocalDateTime sentAt;
}
