package pl.mmilewczyk.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.clients.post.PostRequest;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventRequestToJoinResponse;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.service.EventService;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<EventResponse> getEventById(@RequestParam Long eventId) {
        return status(FOUND).body(eventService.getEventResponseById(eventId));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Page<PrivateEventResponse>> getEventByNameLike(@PathVariable("name") String name) {
        return status(FOUND).body(eventService.getEventByNameLike(name));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createNewEvent(@RequestBody EventRequest eventRequest) {
        return status(CREATED).body(eventService.createNewEvent(eventRequest));
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteEventById(@PathVariable Long eventId) {
        eventService.deleteEventById(eventId);
    }

    @PutMapping("/edit/add/moderator")
    public ResponseEntity<EventResponse> makeSomeoneAModerator(@RequestParam Long eventId, @RequestParam Long userId) {
        return status(OK).body(eventService.makeSomeoneAModerator(eventId, userId));
    }

    @PutMapping("/edit/remove/moderator")
    public ResponseEntity<EventResponse> removeSomeoneAsAModerator(@RequestParam Long eventId, @RequestParam Long userId) {
        return status(OK).body(eventService.removeSomeoneAsAModerator(eventId, userId));
    }

    @PutMapping("/edit/{eventId}")
    public ResponseEntity<EventResponse> editEvent(@PathVariable("eventId") Long eventId, @RequestBody EventRequest eventRequest) {
        return status(CREATED).body(eventService.editEvent(eventId, eventRequest));
    }

    @PutMapping("/join/{eventId}")
    public ResponseEntity<Object> joinToEvent(@PathVariable("eventId") Long eventId) {
        return status(OK).body(eventService.joinToEvent(eventId));
    }

    @PutMapping("/leave/{eventId}")
    public ResponseEntity<Object> leaveEvent(@PathVariable("eventId") Long eventId) {
        return status(OK).body(eventService.leaveEvent(eventId));
    }

    @PutMapping("/remove-user")
    public ResponseEntity<EventResponse> removeSomeoneFromEvent(@RequestParam Long eventId,
                                                                @RequestParam Long userToRemoveId) {
        return status(ACCEPTED).body(eventService.removeSomeoneFromEvent(eventId, userToRemoveId));
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<EventRequestToJoinResponse>> getCurrentUsersRequestsToJoinToPrivateEvent() {
        return status(FOUND).body(eventService.getCurrentUsersRequestsToJoinToPrivateEvent());
    }

    @GetMapping("/requests/{eventId}")
    public ResponseEntity<Page<EventRequestToJoinResponse>> getPendingRequestsToPrivateJoin(@PathVariable("eventId") Long eventId) {
        return status(FOUND).body(eventService.getPendingRequestsToPrivateJoin(eventId));
    }

    @PostMapping("/requests")
    public ResponseEntity<PrivateEventResponse> requestToJoinToPrivateEvent(@RequestParam Long eventId) {
        return status(CREATED).body(eventService.requestToJoinToPrivateEvent(eventId));
    }

    @PutMapping("requests/accept")
    public ResponseEntity<EventResponse> acceptRequestToJoinToPrivateEvent(@RequestParam Long eventRequestToJoinId) {
        return status(OK).body(eventService.acceptRequestToJoinToPrivateEvent(eventRequestToJoinId));
    }

    @PutMapping("requests/reject")
    public ResponseEntity<PrivateEventResponse> rejectRequestToJoinToPrivateEvent(@RequestParam Long eventRequestToJoinId) {
        return status(OK).body(eventService.rejectRequestToJoinToPrivateEvent(eventRequestToJoinId));
    }

    @PutMapping("{eventId}/create/post")
    public ResponseEntity<EventResponse> addPostToGroup(@PathVariable("eventId") Long eventId,
                                                        @RequestBody PostRequest postRequest) {
        return status(CREATED).body(eventService.addPostToEvent(eventId, postRequest));
    }
}


