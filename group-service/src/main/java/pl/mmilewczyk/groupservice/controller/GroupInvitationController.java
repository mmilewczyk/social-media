package pl.mmilewczyk.groupservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.mmilewczyk.groupservice.model.dto.GroupInvitationRequest;
import pl.mmilewczyk.groupservice.service.GroupInvitationService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/groups/invitations")
public class GroupInvitationController {

    private final GroupInvitationService groupInvitationService;

    @PostMapping("/invite")
    public ResponseEntity<GroupInvitationRequest> inviteSomeoneToGroup(@RequestParam Long groupId, @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupInvitationService.inviteSomeoneToGroup(groupId, userId));
    }
}
