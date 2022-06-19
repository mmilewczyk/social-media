package pl.mmilewczyk.eventservice.model.entity;

import lombok.*;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
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
    private List<Long> moderatorsIds;

    @ElementCollection
    private List<Long> postsIds;

    public Event(String name, LocalDateTime startAt, LocalDateTime endAt, String location, Long organizerId, Boolean isPrivate, String description, List<String> hashtags, List<Long> attendeesIds, List<Long> moderatorsIds, List<Long> postsIds) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.organizerId = organizerId;
        this.isPrivate = isPrivate;
        this.description = description;
        this.hashtags = hashtags;
        this.attendeesIds = attendeesIds;
        this.moderatorsIds = moderatorsIds;
        this.postsIds = postsIds;
    }

    public Boolean areDatesCorrect() {
        return this.startAt.isBefore(this.endAt);
    }

    public EventResponse mapEventToEventResponse(UserResponseWithId organizer,
                                                 List<UserResponseWithId> attendees,
                                                 List<UserResponseWithId> moderators,
                                                 List<PostResponse> posts) {
        return new EventResponse(
                this.eventId,
                this.name,
                this.startAt,
                this.endAt,
                this.location,
                organizer,
                this.isPrivate,
                this.description,
                this.hashtags,
                attendees,
                moderators,
                posts);

    }

    public PrivateEventResponse mapEventToPrivateEventResponse(UserResponseWithId organizer) {
        return new PrivateEventResponse(
                this.eventId,
                this.name,
                this.startAt,
                this.endAt,
                this.location,
                organizer,
                this.isPrivate,
                this.description,
                this.hashtags,
                (long) this.attendeesIds.size(),
                (long) this.postsIds.size());

    }

    public boolean isUserAMemberOfEvent(UserResponseWithId userResponseWithId) {
        return this.getAttendeesIds().contains(userResponseWithId.userId());
    }
}
