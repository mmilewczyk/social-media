package pl.mmilewczyk.groupservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.groupservice.model.dto.GroupInvitationRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.service.GroupInvitationService;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/groups/invitations")
public class GroupInvitationController {

    private final GroupInvitationService groupInvitationService;

    @PostMapping("/invite")
    public ResponseEntity<GroupInvitationRequest> inviteSomeoneToGroup(@RequestParam Long groupId, @RequestParam Long userId) {
        return status(CREATED).body(groupInvitationService.inviteSomeoneToGroup(groupId, userId));
    }

    @PutMapping("/accept")
    public ResponseEntity<GroupResponse> acceptInvitation(@RequestParam Long groupInvitationId) {
        return status(ACCEPTED).body(groupInvitationService.acceptInvitationToGroup(groupInvitationId));
    }

    @PutMapping("/reject")
    public ResponseEntity<GroupResponse> rejectInvitation(@RequestParam Long groupInvitationId) {
        return status(ACCEPTED).body(groupInvitationService.rejectInvitationToGroup(groupInvitationId));
    }
}
