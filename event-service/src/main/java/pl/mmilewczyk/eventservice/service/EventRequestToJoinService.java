package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventRequestToJoinResponse;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.model.entity.Event;
import pl.mmilewczyk.eventservice.model.entity.EventRequestToJoin;
import pl.mmilewczyk.eventservice.model.enums.Status;
import pl.mmilewczyk.eventservice.repository.EventRepository;
import pl.mmilewczyk.eventservice.repository.EventRequestToJoinRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventRequestToJoinService {

    private final UtilsService utilsService;
    private final EventRequestToJoinRepository eventRequestToJoinRepository;
    private final EventService eventService;
    private final EventRepository eventRepository;

    private static final String EVENT_REQUEST_TO_JOIN_NOT_FOUND_ALERT = "The requested event request to join with id %s was not found.";

    public Page<EventRequestToJoinResponse> getCurrentUsersRequestsToJoinToPrivateEvent() {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        List<EventRequestToJoin> eventRequestToJoins = eventRequestToJoinRepository
                .getEventRequestToJoinsByPersonJoiningId(currentUser.userId());
        List<EventRequestToJoinResponse> responses = new ArrayList<>();
        eventRequestToJoins.forEach(eventRequestToJoin -> {
            EventResponse event = eventService.getEventResponseById(eventRequestToJoin.getEventId());
            UserResponseWithId userJoining = utilsService.getUserById(eventRequestToJoin.getPersonJoiningId());
            responses.add(eventRequestToJoin.mapEventRequestToJoinToResponse(event, userJoining));
        });
        return new PageImpl<>(responses);
    }

    public Page<EventRequestToJoinResponse> getPendingRequestsToPrivateJoin(Long eventId) {
        EventResponse event = eventService.getEventResponseById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.moderators().contains(currentUser) || !event.organizer().userId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be a moderator to view requests to join the event");
        }
        List<EventRequestToJoin> eventRequestToJoinList = eventRequestToJoinRepository
                .getEventRequestToJoinsByStatusAndEventId(Status.PENDIND, eventId);
        List<EventRequestToJoinResponse> responses = new ArrayList<>();
        eventRequestToJoinList.forEach(eventRequestToJoin -> {
            UserResponseWithId userJoining = utilsService.getUserById(eventRequestToJoin.getPersonJoiningId());
            responses.add(eventRequestToJoin.mapEventRequestToJoinToResponse(event, userJoining));
        });
        return new PageImpl<>(responses);
    }

    public PrivateEventResponse requestToJoinToPrivateEvent(Long eventId) {
        EventResponse event = eventService.getEventResponseById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();

        if (event.attendees().contains(currentUser) || event.organizer().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are already a participant of the event");
        }
        EventRequestToJoin eventRequestToJoin = new EventRequestToJoin(eventId, currentUser.userId(), Status.PENDIND);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return eventService.getPrivateEventResponseById(eventId);
    }

    public EventResponse acceptRequestToJoinToPrivateEvent(Long eventRequestToJoinId) {
        EventRequestToJoin eventRequestToJoin = getEventRequestToJoinById(eventRequestToJoinId);
        Event event = eventService.getEventById(eventRequestToJoin.getEventId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.getModeratorsIds().contains(currentUser.userId()) || !event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to accept requests to join the event");
        }
        List<Long> attendees = event.getAttendeesIds();
        if (attendees.contains(eventRequestToJoin.getPersonJoiningId())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user is already a participant in the event");
        }
        attendees.add(eventRequestToJoin.getPersonJoiningId());
        event.setAttendeesIds(attendees);
        eventRepository.save(event);
        eventRequestToJoin.setStatus(Status.ACCEPTED);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return eventService.getEventResponseById(event.getEventId());
    }

    public PrivateEventResponse rejectRequestToJoinToPrivateEvent(Long eventRequestToJoinId) {
        EventRequestToJoin eventRequestToJoin = getEventRequestToJoinById(eventRequestToJoinId);
        Event event = eventService.getEventById(eventRequestToJoin.getEventId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.getModeratorsIds().contains(currentUser.userId()) || !event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to accept requests to join the event");
        }
        if (event.getAttendeesIds().contains(eventRequestToJoin.getPersonJoiningId())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user is already a participant in the event");
        }
        eventRequestToJoin.setStatus(Status.REJECTED);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return eventService.getPrivateEventResponseById(event.getEventId());
    }

    private EventRequestToJoin getEventRequestToJoinById(Long eventRequestToJoinId) {
        return eventRequestToJoinRepository.findById(eventRequestToJoinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(EVENT_REQUEST_TO_JOIN_NOT_FOUND_ALERT, eventRequestToJoinId)));
    }
}
