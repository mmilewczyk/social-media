package pl.mmilewczyk.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.eventservice.model.dto.EventRequestToJoinResponse;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.service.EventRequestToJoinService;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("api/v1/events/requests")
@RequiredArgsConstructor
public class EventRequestToJoinController {

    private final EventRequestToJoinService requestToJoinService;

    @GetMapping
    public ResponseEntity<Page<EventRequestToJoinResponse>> getCurrentUsersRequestsToJoinToPrivateEvent() {
        return status(FOUND).body(requestToJoinService.getCurrentUsersRequestsToJoinToPrivateEvent());
    }

    @GetMapping
    public ResponseEntity<Page<EventRequestToJoinResponse>> getPendingRequestsToPrivateJoin(@RequestParam Long eventId) {
        return status(FOUND).body(requestToJoinService.getPendingRequestsToPrivateJoin(eventId));
    }

    @PostMapping
    public ResponseEntity<PrivateEventResponse> requestToJoinToPrivateEvent(@RequestParam Long eventId) {
        return status(CREATED).body(requestToJoinService.requestToJoinToPrivateEvent(eventId));
    }

    @PutMapping("/accept")
    public ResponseEntity<EventResponse> acceptRequestToJoinToPrivateEvent(@RequestParam Long eventRequestToJoinId) {
        return status(OK).body(requestToJoinService.acceptRequestToJoinToPrivateEvent(eventRequestToJoinId));
    }

    @PutMapping("/reject")
    public ResponseEntity<PrivateEventResponse> rejectRequestToJoinToPrivateEvent(@RequestParam Long eventRequestToJoinId) {
        return status(OK).body(requestToJoinService.rejectRequestToJoinToPrivateEvent(eventRequestToJoinId));
    }
}


