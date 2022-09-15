package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventInvitationRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.model.entity.EventInvitation;
import pl.mmilewczyk.eventservice.repository.EventInvitationRepository;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.*;
import static pl.mmilewczyk.eventservice.model.enums.Status.ACCEPTED;
import static pl.mmilewczyk.eventservice.model.enums.Status.*;

@RequiredArgsConstructor
@Service
public class EventInvitationService {

    private final UtilsService utilsService;
    private final EventInvitationRepository eventInvitationRepository;
    private final EventService eventService;
    private final NotificationClient notificationClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    private static final String EVENT_INVITATION_NOT_FOUND_ALERT = "The requested event invitation with id %s was not found.";

    public EventInvitationRequest inviteSomeoneToEvent(Long eventId, Long userId) {
        UserResponseWithId inviter = utilsService.getCurrentUser();
        UserResponseWithId invitee = utilsService.getUserById(userId);
        EventResponse event = eventService.getEventResponseById(eventId);
        EventInvitation eventInvitation = null;
        if (invitee != null && inviter != null && event != null) {
            if (inviter.userId().equals(invitee.userId())) {
                throw new ResponseStatusException(UNAUTHORIZED, "You cannot invite yourself to the event");
            } else if (event.attendees().contains(invitee)) {
                throw new ResponseStatusException(UNAUTHORIZED, format(
                        "User %s is aldread a member of the event %s", invitee.username(), eventId));
            } else if (!event.attendees().contains(inviter)) {
                throw new ResponseStatusException(UNAUTHORIZED, format(
                        "You have to be a member of the event %s to invite other users", eventId));
            } else {
                eventInvitation = new EventInvitation(
                        eventId, inviter.userId(), invitee.userId(), INVITED);
                eventInvitationRepository.save(eventInvitation);
                sendEmailToTheInviteeAboutInvitationToTheEvent(eventId, eventInvitation);
            }
        }
        assert eventInvitation != null;
        return eventInvitation.mapEventInvitationToEventInvitationRequest(event, inviter, invitee);
    }

    private void sendEmailToTheInviteeAboutInvitationToTheEvent(Long eventId, EventInvitation eventInvitation) {
        EventResponse eventResponse = eventService.getEventResponseById(eventId);
        UserResponseWithId eventInvitee = utilsService.getUserById(eventInvitation.getInviteeId());
        NotificationRequest notificationRequest = new NotificationRequest(
                eventInvitee.userId(),
                eventInvitee.email(),
                format("Hi %s! You are invited to the event '%s'.",
                        eventInvitee.username(), eventResponse.name()));
        notificationClient.sendEmailToTheInviteeAboutInvitationToTheEvent(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }

    public EventResponse acceptInvitationToEvent(Long eventInvitationId) {
        EventInvitation eventInvitation = eventInvitationRepository.findById(eventInvitationId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND,
                        format(EVENT_INVITATION_NOT_FOUND_ALERT, eventInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (eventInvitation.getInviteeId().equals(currentUser.userId())) {
            switch (eventInvitation.getStatus()) {
                case REJECTED -> throw new ResponseStatusException(NOT_ACCEPTABLE,
                        "You have already rejected the invitation to event " + eventInvitation.getEventId());
                case INVITED -> {
                    eventInvitation.setStatus(ACCEPTED);
                    eventInvitationRepository.save(eventInvitation);
                    eventService.joinToEventByInvitation(eventInvitation.getEventId());
                }
                case ACCEPTED -> throw new ResponseStatusException(NOT_ACCEPTABLE,
                        "You have already accepted the invitation to event " + eventInvitation.getEventId());
            }
        } else {
            throw new ResponseStatusException(UNAUTHORIZED,
                    currentUser.username() + ", you are not invited to event " + eventInvitation.getEventId());
        }
        return eventService.getEventResponseById(eventInvitation.getEventId());
    }

    public PrivateEventResponse rejectInvitationToEvent(Long eventInvitationId) {
        EventInvitation eventInvitation = eventInvitationRepository.findById(eventInvitationId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND,
                        format(EVENT_INVITATION_NOT_FOUND_ALERT, eventInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (eventInvitation.getInviteeId().equals(currentUser.userId()) || utilsService.isUserAdminOrModerator(currentUser)) {
            switch (eventInvitation.getStatus()) {
                case REJECTED -> throw new ResponseStatusException(NOT_ACCEPTABLE,
                        "You have already rejected the invitation to event " + eventInvitation.getEventId());
                case INVITED -> {
                    eventInvitation.setStatus(REJECTED);
                    eventInvitationRepository.save(eventInvitation);
                }
                case ACCEPTED -> throw new ResponseStatusException(NOT_ACCEPTABLE,
                        "You have already accepted the invitation to event " + eventInvitation.getEventId());
            }
        } else {
            throw new ResponseStatusException(UNAUTHORIZED,
                    currentUser.username() + ", you are not invited to event " + eventInvitation.getEventInvitationId());
        }
        return eventService.getPrivateEventResponseById(eventInvitation.getEventId());
    }

    public Page<EventInvitationRequest> getCurrentUsersInvitationsToEvent() {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        List<EventInvitation> eventInvitationRequests = eventInvitationRepository
                .findEventInvitationsByInviteeId(currentUser.userId());
        List<EventInvitationRequest> responses = new LinkedList<>();
        eventInvitationRequests.forEach(eventInvitationRequest -> {
            EventResponse event = eventService.getEventResponseById(eventInvitationRequest.getEventId());
            UserResponseWithId inviter = utilsService.getUserById(eventInvitationRequest.getInviterId());
            UserResponseWithId invitee = utilsService.getUserById(eventInvitationRequest.getInviteeId());
            responses.add(eventInvitationRequest.mapEventInvitationToEventInvitationRequest(event, inviter, invitee));
        });
        return new PageImpl<>(responses);
    }
}
