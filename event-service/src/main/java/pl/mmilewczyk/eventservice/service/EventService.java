package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
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

    private EventResponse getEventResponseById(Long eventId) {
        Event event = getEventById(eventId);
        UserResponseWithId organizer = utilsService.getUserById(event.getOrganizerId());
        List<UserResponseWithId> attendees = getListOfUserResponsesByIds(event.getAttendeesIds());
        List<UserResponseWithId> moderators = getListOfUserResponsesByIds(event.getModeratorsIds());
        List<PostResponse> posts = getListOfPostResponsesByIds(event.getPostsIds());
        return event.mapEventToEventResponse(organizer, attendees, moderators, posts);
    }

    private Event getEventById(Long eventId) {
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
