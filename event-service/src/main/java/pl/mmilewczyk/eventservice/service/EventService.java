package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.model.entity.Event;
import pl.mmilewczyk.eventservice.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final UtilsService utilsService;
    private final EventRepository eventRepository;
    private final EventRequestToJoinService eventRequestToJoinService;

    private static final String EVENT_NOT_FOUND_ALERT = "The requested event with id %s was not found.";


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
                .postsIds(Collections.emptyList())
                .build();

        if (!newEvent.areDatesCorrect()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Incorrect dates");
        }
        eventRepository.save(newEvent);
        return getEventResponseById(newEvent.getEventId());
    }

    public void deleteEventById(Long eventId) {
        Event event = getEventById(eventId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (!currentUser.userId().equals(event.getOrganizerId()) || !utilsService.isUserAdminOrModerator(currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You must be the event organizer or application moderator to delete this event.");
        }
        eventRepository.delete(event);
    }

    public EventResponse makeSomeoneAModerator(Long eventId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!event.getOrganizerId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner of the event");
        }
        if (event.getModeratorsIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner of the event");
        }
        if (!event.getModeratorsIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(
                    "User %s is aldread not a moderator of the event %s", user.username(), eventId));
        }
        event.getModeratorsIds().remove(userId);
        eventRepository.save(event);

        return getEventResponseById(eventId);
    }

    public EventResponse editEvent(Long eventId, EventRequest eventRequest) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);

        if (!event.getOrganizerId().equals(currentUser.userId()) ||
                !currentUser.userRole().equals(RoleName.MODERATOR) ||
                !currentUser.userRole().equals(RoleName.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the organizer can edit the event");
        }
        event.setName(eventRequest.name());
        event.setStartAt(eventRequest.startAt());
        event.setEndAt(eventRequest.endAt());
        event.setLocation(eventRequest.location());
        event.setIsPrivate(eventRequest.isPrivate());
        event.setDescription(eventRequest.description());
        event.setHashtags(eventRequest.hashtags());
        eventRepository.saveAndFlush(event);
        if (event.getIsPrivate().booleanValue() == Boolean.FALSE) {
            eventRequestToJoinService.getPendingRequestsToPrivateJoin(eventId).get()
                    .forEach(request -> eventRequestToJoinService
                            .acceptRequestToJoinToPrivateEvent(request.eventRequestToJoinId()));
        }
        return getEventResponseById(eventId);
    }

    public Object joinToEvent(Long eventId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (event.isUserAMemberOfEvent(currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are already member of the event %s", eventId));
        }
        if (event.getIsPrivate()) {
            eventRequestToJoinService.requestToJoinToPrivateEvent(eventId);
            return getPrivateEventResponseById(eventId);
        } else {
            event.getAttendeesIds().add(currentUser.userId());
            eventRepository.save(event);
            return getEventResponseById(eventId);
        }
    }

    public Object leaveEvent(Long eventId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Event event = getEventById(eventId);
        if (!event.isUserAMemberOfEvent(currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are already not a member of the event %s", eventId));
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
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user you are trying to remove is not a member of the event");
        }
        if (event.getModeratorsIds().contains(userToRemoveId) || event.getOrganizerId().equals(userToRemoveId)) {
            if (!event.getOrganizerId().equals(currentUser.userId())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not the event owner to remove a moderator");
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

    protected Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(EVENT_NOT_FOUND_ALERT, eventId)));
    }

    private List<UserResponseWithId> getListOfUserResponsesByIds(List<Long> userIds) {
        return userIds.stream()
                .map(utilsService::getUserById)
                .collect(Collectors.toList());
    }

    private List<PostResponse> getListOfPostResponsesByIds(List<Long> postIds) {
        return postIds.stream()
                .map(utilsService::getPostById)
                .collect(Collectors.toList());
    }
}
