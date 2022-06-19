package pl.mmilewczyk.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.eventservice.model.dto.EventInvitationRequest;
import pl.mmilewczyk.eventservice.model.dto.EventResponse;
import pl.mmilewczyk.eventservice.model.dto.PrivateEventResponse;
import pl.mmilewczyk.eventservice.service.EventInvitationService;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("api/v1/events/invitations")
@RequiredArgsConstructor
public class EventInvitationController {

    private final EventInvitationService invitationService;

    @GetMapping
    public ResponseEntity<Page<EventInvitationRequest>> getCurrentUsersInvitationsToEvent() {
        return status(FOUND).body(invitationService.getCurrentUsersInvitationsToEvent());
    }

    @PostMapping("/invite")
    public ResponseEntity<EventInvitationRequest> inviteSomeoneToEvent(@RequestParam Long eventId,
                                                                       @RequestParam Long userId) {
        return status(CREATED).body(invitationService.inviteSomeoneToEvent(eventId, userId));
    }

    @PutMapping("/accept")
    public ResponseEntity<EventResponse> acceptInvitationToEvent(@RequestParam Long eventInvitationId) {
        return status(ACCEPTED).body(invitationService.acceptInvitationToEvent(eventInvitationId));
    }

    @PutMapping("/reject")
    public ResponseEntity<PrivateEventResponse> rejectInvitationToEvent(@RequestParam Long eventInvitationId) {
        return status(ACCEPTED).body(invitationService.rejectInvitationToEvent(eventInvitationId));
    }
}
