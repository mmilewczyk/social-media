package pl.mmilewczyk.eventservice.model.entity;

import lombok.*;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventInvitationRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.enums.Status;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import static java.util.Objects.requireNonNull;
import static javax.persistence.GenerationType.SEQUENCE;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventInvitation {

    @Id
    @SequenceGenerator(name = "event_invitation_id_sequence", sequenceName = "event_invitation_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "event_invitation_id_sequence")
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
            throw new ResponseStatusException(NOT_ACCEPTABLE);
        }
        return new EventInvitationRequest(this.eventInvitationId,
                requireNonNull(event).eventId(),
                event.name(),
                requireNonNull(inviter).userId(),
                inviter.username(),
                invitee.userId(),
                invitee.username());

    }
}
