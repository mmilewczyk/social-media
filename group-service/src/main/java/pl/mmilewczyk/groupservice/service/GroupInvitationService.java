package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.groupservice.model.dto.GroupInvitationRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.model.entity.GroupInvitation;
import pl.mmilewczyk.groupservice.model.enums.InvitationStatus;
import pl.mmilewczyk.groupservice.repository.GroupInvitationRepository;

@RequiredArgsConstructor
@Service
public class GroupInvitationService {

    private final UtilsService utilsService;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupService groupService;

    public GroupInvitationRequest inviteSomeoneToGroup(Long groupId, Long userId) {
        UserResponseWithId inviter = utilsService.getCurrentUser();
        UserResponseWithId invitee = utilsService.getUserById(userId);
        GroupResponse group = groupService.getGroupResponseById(groupId);
        GroupInvitation groupInvitation = null;
        if (invitee != null && inviter != null && group != null) {
            if (inviter.userId().equals(invitee.userId())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot invite yourself to the group");
            } else if (group.members().contains(invitee)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(
                        "User %s is aldread a member of the group %s", invitee.username(), groupId));
            } else if (group.members().contains(inviter)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format(
                        "You have to be a member of the group %s to invite other users", groupId));
            } else {
                groupInvitation = new GroupInvitation(
                        groupId,
                        inviter.userId(),
                        invitee.userId(),
                        InvitationStatus.INVITED);
                groupInvitationRepository.save(groupInvitation);
            }
            // TODO: SEND NOTIFICATION TO INVITEE
        }
        assert groupInvitation != null;
        return groupInvitation.mapGroupInvitationToGroupInvitationRequest(group, inviter, invitee);
    }
}
