package pl.mmilewczyk.eventservice.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Event {

    @Id
    @Column(name = "event_id", nullable = false)
    @SequenceGenerator(name = "event_id_sequence", sequenceName = "event_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_sequence")
    private Long eventId;

    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String location;
    private Long organizerId;
    private Boolean isPrivate;
    private String description;

    @ElementCollection
    private List<String> hashtags;

    @ElementCollection
    private List<Long> attendeesIds;

    @ElementCollection
    private List<Long> postsIds;
}
