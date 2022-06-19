package pl.mmilewczyk.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.service.EventService;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

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
}
