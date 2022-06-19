package pl.mmilewczyk.eventservice.model.entity;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventInvitationRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.enums.Status;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventInvitation {

    @Id
    @SequenceGenerator(name = "event_invitation_id_sequence", sequenceName = "event_invitation_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_invitation_id_sequence")
    private Long eventInvitationId;

    private Long eventId;
    private Long inviterId;
    private Long inviteeId;
    private Status status;

    public EventInvitation(Long eventId, Long inviterId, Long inviteeId, Status status) {
        this.eventId = eventId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.status = status;
    }

    public EventInvitationRequest mapEventInvitationToEventInvitationRequest(EventResponse event, UserResponseWithId inviter, UserResponseWithId invitee) {
        if (event == null && inviter == null && invitee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return new EventInvitationRequest(this.getEventId(), event.name(), inviter.userId(), inviter.username(), invitee.userId(), invitee.username());

    }
}
