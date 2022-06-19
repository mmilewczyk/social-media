package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventInvitationRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.model.entity.EventInvitation;
import pl.mmilewczyk.eventservice.model.enums.Status;
import pl.mmilewczyk.eventservice.repository.EventInvitationRepository;

@RequiredArgsConstructor
@Service
public class EventInvitationService {

    private final UtilsService utilsService;
    private final EventInvitationRepository eventInvitationRepository;
    private final EventService eventService;

    private static final String EVENT_INVITATION_NOT_FOUND_ALERT = "The requested event invitation with id %s was not found.";

    public EventInvitationRequest inviteSomeoneToEvent(Long eventId, Long userId) {
        UserResponseWithId inviter = utilsService.getCurrentUser();
        UserResponseWithId invitee = utilsService.getUserById(userId);
        EventResponse event = eventService.getEventResponseById(eventId);
        EventInvitation eventInvitation = null;
        if (invitee != null && inviter != null && event != null) {
            if (inviter.userId().equals(invitee.userId())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot invite yourself to the event");
            } else if (event.attendees().contains(invitee)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(
                        "User %s is aldread a member of the event %s", invitee.username(), eventId));
            } else if (event.attendees().contains(inviter)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(
                        "You have to be a member of the event %s to invite other users", eventId));
            } else {
                eventInvitation = new EventInvitation(
                        eventId,
                        inviter.userId(),
                        invitee.userId(),
                        Status.INVITED);
                eventInvitationRepository.save(eventInvitation);
            }
            // TODO: SEND NOTIFICATION TO INVITEE
        }
        assert eventInvitation != null;
        return eventInvitation.mapEventInvitationToEventInvitationRequest(event, inviter, invitee);
    }

    public EventResponse acceptInvitationToEvent(Long eventInvitationId) {
        EventInvitation eventInvitation = eventInvitationRepository.findById(eventInvitationId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(EVENT_INVITATION_NOT_FOUND_ALERT, eventInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!eventInvitation.getInviteeId().equals(currentUser.userId()) || !utilsService.isUserAdminOrModerator(currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not invited to event " + eventInvitation.getEventId());
        }
        switch (eventInvitation.getStatus()) {
            case REJECTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You have already rejected the invitation to event " + eventInvitation.getEventId());
            case INVITED -> {
                eventInvitation.setStatus(Status.ACCEPTED);
                eventInvitationRepository.save(eventInvitation);
                eventService.joinToEvent(eventInvitation.getEventId());
            }
            case ACCEPTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You have already accepted the invitation to event " + eventInvitation.getEventId());
        }
        return eventService.getEventResponseById(eventInvitation.getEventId());
    }

    public PrivateEventResponse rejectInvitationToEvent(Long eventInvitationId) {
        EventInvitation eventInvitation = eventInvitationRepository.findById(eventInvitationId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(EVENT_INVITATION_NOT_FOUND_ALERT, eventInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!eventInvitation.getInviteeId().equals(currentUser.userId()) || !utilsService.isUserAdminOrModerator(currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not invited to event " + eventInvitation.getEventInvitationId());
        }
        switch (eventInvitation.getStatus()) {
            case REJECTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You have already rejected the invitation to event " + eventInvitation.getEventId());
            case INVITED -> {
                eventInvitation.setStatus(Status.REJECTED);
                eventInvitationRepository.save(eventInvitation);
                eventService.joinToEvent(eventInvitation.getEventId());
            }
            case ACCEPTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You have already accepted the invitation to event " + eventInvitation.getEventId());
        }
        return eventService.getPrivateEventResponseById(eventInvitation.getEventId());
    }
}
