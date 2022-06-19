package pl.mmilewczyk.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.eventservice.model.dto.EventRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.service.EventService;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createNewEvent(@RequestBody EventRequest eventRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createNewEvent(eventRequest));
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventById(@PathVariable Long eventId) {
        eventService.deleteEventById(eventId);
    }
}
