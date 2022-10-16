package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostRequest;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventRequestToJoinResponse;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.model.entity.Event;
import pl.mmilewczyk.eventservice.model.entity.EventRequestToJoin;
import pl.mmilewczyk.eventservice.repository.EventRepository;
import pl.mmilewczyk.eventservice.repository.EventRequestToJoinRepository;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.*;
import static pl.mmilewczyk.eventservice.model.enums.Status.ACCEPTED;
import static pl.mmilewczyk.eventservice.model.enums.Status.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final UtilsService utilsService;
    private final EventRepository eventRepository;
    private final EventRequestToJoinRepository eventRequestToJoinRepository;

    private static final String EVENT_NOT_FOUND_ALERT = "The requested event with id %s was not found.";
    private static final String EVENT_REQUEST_TO_JOIN_NOT_FOUND_ALERT = "The requested event request to join with id %s was not found.";

    public Page<PrivateEventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        List<PrivateEventResponse> mappedEvents = mapEventsToPrivateEventResponseList(events);
        return new PageImpl<>(mappedEvents);
    }

    public Page<PrivateEventResponse> getEventByNameLike(String name) {
        List<Event> events = eventRepository.findAllByNameLikeIgnoreCase(name);
        List<PrivateEventResponse> mappedEvents = mapEventsToPrivateEventResponseList(events);
        return new PageImpl<>(mappedEvents);
    }

    private List<PrivateEventResponse> mapEventsToPrivateEventResponseList(List<Event> events) {
        List<PrivateEventResponse> mappedEvents = new LinkedList<>();
        events.forEach(event -> {
            UserResponseWithId user = utilsService.getUserById(event.getOrganizerId());
            mappedEvents.add(event.mapEventToPrivateEventResponse(user));
        });
        return mappedEvents;
    }

    public EventResponse createNewEvent(EventRequest eventRequest) {
        UserResponseWithId organizer = utilsService.getCurrentUser();

        Event newEvent = Event.builder()
                .name(eventRequest.name())
                .startAt(eventRequest.startAt())
                .endAt(eventRequest.endAt())
                .location(eventRequest.location())
                .organizerId(organizer.userId())
                .isPrivate(eventRequest.isPrivate())
                .description(eventRequest.description())
                .hashtags(eventRequest.hashtags())
                .attendeesIds(List.of(organizer.userId()))
                .moderatorsIds(List.of(organizer.userId()))
                .postsIds(emptyList())
                .build();

        if (!newEvent.areDatesCorrect()) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Incorrect dates");
        }
        eventRepository.save(newEvent);
        return getEventResponseById(newEvent.getEventId());
    }

    public void deleteEventById(Long eventId) {
        Event event = getEventById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!isEventAdminOrModerator(currentUser, event)) {
            throw new ResponseStatusException(UNAUTHORIZED,
                    "You must be the event organizer or application moderator to delete this event.");
        }
        eventRepository.delete(event);
    }

    public EventResponse makeSomeoneAModerator(Long eventId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "You are not owner of the event");
        }
        if (event.getModeratorsIds().contains(userId)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, format(
                    "User %s is aldread a moderator of the event %s", user.username(), eventId));
        }
        event.getModeratorsIds().add(userId);
        eventRepository.save(event);

        return getEventResponseById(eventId);
    }

    public EventResponse removeSomeoneAsAModerator(Long eventId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "You are not owner of the event");
        }
        if (!event.getModeratorsIds().contains(userId)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, format(
                    "User %s is aldread not a moderator of the event %s", user.username(), eventId));
        }
        event.getModeratorsIds().remove(userId);
        eventRepository.save(event);

        return getEventResponseById(eventId);
    }

    public EventResponse editEvent(Long eventId, EventRequest eventRequest) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);

        if (!isEventAdminOrModerator(currentUser, event)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Only the organizer can edit the event");
        }
        event.setName(eventRequest.name());
        event.setStartAt(eventRequest.startAt());
        event.setEndAt(eventRequest.endAt());
        event.setLocation(eventRequest.location());
        event.setIsPrivate(eventRequest.isPrivate());
        event.setDescription(eventRequest.description());
        event.setHashtags(eventRequest.hashtags());
        eventRepository.saveAndFlush(event);
        if (FALSE.equals(event.getIsPrivate())) {
            getPendingRequestsToPrivateJoin(eventId).get()
                    .forEach(request -> acceptRequestToJoinToPrivateEvent(request.eventRequestToJoinId()));
        }
        return getEventResponseById(eventId);
    }

    public boolean isEventAdminOrModerator(UserResponseWithId user, Event event) {
        return utilsService.isUserAdminOrModerator(user) ||
                event.getModeratorsIds().contains(user.userId()) ||
                event.getOrganizerId().equals(user.userId());
    }

    public Object joinToEvent(Long eventId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (event.isUserAMemberOfEvent(currentUser)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE,
                    format("You are already member of the event %s", eventId));
        }
        if (TRUE.equals(event.getIsPrivate())) {
            log.info("User {} is trying to join to the private event {}", currentUser.userId(), eventId);
            requestToJoinToPrivateEvent(eventId);
            return getPrivateEventResponseById(eventId);
        } else {
            log.info("User {} is trying to join to the event {}", currentUser.userId(), eventId);
            event.getAttendeesIds().add(currentUser.userId());
            eventRepository.save(event);
            return getEventResponseById(eventId);
        }
    }

    public void joinToEventByInvitation(Long eventId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (event.isUserAMemberOfEvent(currentUser)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE,
                    format("You are already member of the event %s", eventId));
        }
        event.getAttendeesIds().add(currentUser.userId());
        eventRepository.save(event);
        getEventResponseById(eventId);
    }

    public Object leaveEvent(Long eventId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (!event.isUserAMemberOfEvent(currentUser)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE,
                    format("You are already not a member of the event %s", eventId));
        }
        event.getAttendeesIds().remove(currentUser.userId());
        eventRepository.save(event);
        if (event.getIsPrivate()) {
            return getPrivateEventResponseById(eventId);
        }
        return getEventResponseById(eventId);
    }

    public EventResponse removeSomeoneFromEvent(Long eventId, Long userToRemoveId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (!event.getAttendeesIds().contains(userToRemoveId)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "The user you are trying to remove is not a member of the event");
        }
        if (event.getModeratorsIds().contains(userToRemoveId) || event.getOrganizerId().equals(userToRemoveId)) {
            if (!event.getOrganizerId().equals(currentUser.userId())) {
                throw new ResponseStatusException(UNAUTHORIZED, "You are not the event owner to remove a moderator");
            }
            event.getModeratorsIds().remove(userToRemoveId);
            event.getAttendeesIds().remove(userToRemoveId);
            eventRepository.save(event);
        } else if (event.getModeratorsIds().contains(currentUser.userId()) || event.getOrganizerId().equals(currentUser.userId())) {
            event.getModeratorsIds().remove(userToRemoveId);
            event.getAttendeesIds().remove(userToRemoveId);
            eventRepository.save(event);
        }
        return getEventResponseById(eventId);
    }

    public EventResponse getEventResponseById(Long eventId) {
        Event event = getEventById(eventId);
        UserResponseWithId organizer = utilsService.getUserById(event.getOrganizerId());
        List<UserResponseWithId> attendees = getListOfUserResponsesByIds(event.getAttendeesIds());
        List<UserResponseWithId> moderators = getListOfUserResponsesByIds(event.getModeratorsIds());
        List<PostResponse> posts = getListOfPostResponsesByIds(event.getPostsIds());
        return event.mapEventToEventResponse(organizer, attendees, moderators, posts);
    }

    public PrivateEventResponse getPrivateEventResponseById(Long eventId) {
        Event event = getEventById(eventId);
        UserResponseWithId organizer = utilsService.getUserById(event.getOrganizerId());
        return event.mapEventToPrivateEventResponse(organizer);
    }

    public EventResponse addPostToEvent(Long eventId, PostRequest postRequest) {
        Event event = getEventById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (event.isUserAMemberOfEvent(currentUser) || isEventAdminOrModerator(currentUser, event)) {
            PostResponse postResponse = utilsService.createNewPost(postRequest);
            List<Long> postIds = event.getPostsIds();
            postIds.add(postResponse.postId());
            event.setPostsIds(postIds);
            eventRepository.save(event);
        }
        return getEventResponseById(eventId);
    }


    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, format(EVENT_NOT_FOUND_ALERT, eventId)));
    }

    private List<UserResponseWithId> getListOfUserResponsesByIds(List<Long> userIds) {
        return userIds.stream()
                .map(utilsService::getUserById)
                .toList();
    }

    private List<PostResponse> getListOfPostResponsesByIds(List<Long> postIds) {
        return postIds.stream()
                .map(utilsService::getPostById)
                .toList();
    }

    public Page<EventRequestToJoinResponse> getCurrentUsersRequestsToJoinToPrivateEvent() {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        List<EventRequestToJoin> eventRequestToJoins = eventRequestToJoinRepository
                .getEventRequestToJoinsByPersonJoiningId(currentUser.userId());
        List<EventRequestToJoinResponse> responses = new LinkedList<>();
        eventRequestToJoins.forEach(eventRequestToJoin -> {
            EventResponse event = getEventResponseById(eventRequestToJoin.getEventId());
            UserResponseWithId userJoining = utilsService.getUserById(eventRequestToJoin.getPersonJoiningId());
            responses.add(eventRequestToJoin.mapEventRequestToJoinToResponse(event, userJoining));
        });
        return new PageImpl<>(responses);
    }

    public Page<EventRequestToJoinResponse> getPendingRequestsToPrivateJoin(Long eventId) {
        EventResponse event = getEventResponseById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.moderators().contains(currentUser) || !event.organizer().userId().equals(currentUser.userId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "You must be a moderator to view requests to join the event");
        }
        List<EventRequestToJoin> eventRequestToJoinList = eventRequestToJoinRepository
                .getEventRequestToJoinsByStatusAndEventId(PENDIND, eventId);
        List<EventRequestToJoinResponse> responses = new LinkedList<>();
        eventRequestToJoinList.forEach(eventRequestToJoin -> {
            UserResponseWithId userJoining = utilsService.getUserById(eventRequestToJoin.getPersonJoiningId());
            responses.add(eventRequestToJoin.mapEventRequestToJoinToResponse(event, userJoining));
        });
        return new PageImpl<>(responses);
    }

    public PrivateEventResponse requestToJoinToPrivateEvent(Long eventId) {
        EventResponse event = getEventResponseById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();

        if (event.attendees().contains(currentUser) || event.organizer().equals(currentUser)) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "You are already a participant of the event");
        }
        EventRequestToJoin eventRequestToJoin = new EventRequestToJoin(eventId, currentUser.userId(), PENDIND);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return getPrivateEventResponseById(eventId);
    }

    public EventResponse acceptRequestToJoinToPrivateEvent(Long eventRequestToJoinId) {
        EventRequestToJoin eventRequestToJoin = getEventRequestToJoinById(eventRequestToJoinId);
        Event event = getEventById(eventRequestToJoin.getEventId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.getModeratorsIds().contains(currentUser.userId()) || !event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "You are not authorized to accept requests to join the event");
        }
        List<Long> attendees = event.getAttendeesIds();
        if (attendees.contains(eventRequestToJoin.getPersonJoiningId())) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "The user is already a participant in the event");
        }
        attendees.add(eventRequestToJoin.getPersonJoiningId());
        event.setAttendeesIds(attendees);
        eventRepository.save(event);
        eventRequestToJoin.setStatus(ACCEPTED);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return getEventResponseById(event.getEventId());
    }

    public PrivateEventResponse rejectRequestToJoinToPrivateEvent(Long eventRequestToJoinId) {
        EventRequestToJoin eventRequestToJoin = getEventRequestToJoinById(eventRequestToJoinId);
        Event event = getEventById(eventRequestToJoin.getEventId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!event.getModeratorsIds().contains(currentUser.userId()) || !event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "You are not authorized to accept requests to join the event");
        }
        if (event.getAttendeesIds().contains(eventRequestToJoin.getPersonJoiningId())) {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "The user is already a participant in the event");
        }
        eventRequestToJoin.setStatus(REJECTED);
        eventRequestToJoinRepository.save(eventRequestToJoin);
        return getPrivateEventResponseById(event.getEventId());
    }

    private EventRequestToJoin getEventRequestToJoinById(Long eventRequestToJoinId) {
        return eventRequestToJoinRepository.findById(eventRequestToJoinId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        format(EVENT_REQUEST_TO_JOIN_NOT_FOUND_ALERT, eventRequestToJoinId)));
    }
}
