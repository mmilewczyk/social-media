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

        if (newEvent.areDatesCorrect()) {
            eventRepository.save(newEvent);
            List<UserResponseWithId> attendees = getListOfUserResponsesByIds(newEvent.getAttendeesIds());
            List<UserResponseWithId> moderators = getListOfUserResponsesByIds(newEvent.getModeratorsIds());
            List<PostResponse> posts = getListOfPostResponsesByIds(newEvent.getPostsIds());
            return newEvent.mapEventToEventResponse(organizer, attendees, moderators, posts);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Incorrect dates");
        }
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
