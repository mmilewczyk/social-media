package pl.mmilewczyk.eventservice.model.entity;

import lombok.*;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventRequestToJoinResponse;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.enums.Status;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventRequestToJoin {

    @Id
    @SequenceGenerator(name = "event_request_to_join_id_sequence", sequenceName = "event_request_to_join_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "event_request_to_join_id_sequence")
    private Long eventRequestToJoinId;

    private Long eventId;
    private Long personJoiningId;
    private Status status;

    public EventRequestToJoin(Long eventId, Long personJoiningId, Status status) {
        this.eventId = eventId;
        this.personJoiningId = personJoiningId;
        this.status = status;
    }

    public EventRequestToJoinResponse mapEventRequestToJoinToResponse(EventResponse event, UserResponseWithId personJoining) {
        return new EventRequestToJoinResponse(this.eventRequestToJoinId, this.eventId, event.name(), personJoining, this.status);
    }
}
