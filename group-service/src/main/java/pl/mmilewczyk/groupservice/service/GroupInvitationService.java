package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
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
    private final NotificationClient notificationClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    private static final String GROUP_INVITATION_NOT_FOUND_ALERT = "The requested group invitation with id %s was not found.";

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
                sendEmailToTheInviteeAboutInvitationToTheGroup(groupId, groupInvitation);
            }
        }
        assert groupInvitation != null;
        return groupInvitation.mapGroupInvitationToGroupInvitationRequest(group, inviter, invitee);
    }

    private void sendEmailToTheInviteeAboutInvitationToTheGroup(Long groupId, GroupInvitation groupInvitation) {
        GroupResponse groupResponse = groupService.getGroupResponseById(groupId);
        UserResponseWithId groupInvitee = utilsService.getUserById(groupInvitation.getInviteeId());
        NotificationRequest notificationRequest = new NotificationRequest(
                groupInvitee.userId(),
                groupInvitee.email(),
                String.format("Hi %s! You are invited to the group '%s'.",
                        groupInvitee.username(), groupResponse.groupName()));
        notificationClient.sendEmailToTheInviteeAboutInvitationToTheGroup(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }

    public GroupResponse acceptInvitationToGroup(Long groupInvitationId) {
        GroupInvitation groupInvitation = groupInvitationRepository.findById(groupInvitationId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_INVITATION_NOT_FOUND_ALERT, groupInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (groupInvitation.getInviteeId().equals(currentUser.userId()) || utilsService.isUserAdminOrModerator(currentUser)) {
            switch (groupInvitation.getStatus()) {
                case REJECTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "You have already rejected the invitation to group " + groupInvitation.getGroupId());
                case INVITED -> {
                    groupInvitation.setStatus(InvitationStatus.ACCEPTED);
                    groupInvitationRepository.save(groupInvitation);
                    groupService.joinToGroup(groupInvitation.getGroupId());
                }
                case ACCEPTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "You have already accepted the invitation to group " + groupInvitation.getGroupId());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not invited to group " + groupInvitation.getGroupId());
        }
        return groupService.getGroupResponseById(groupInvitation.getGroupId());
    }

    public GroupResponse rejectInvitationToGroup(Long groupInvitationId) {
        GroupInvitation groupInvitation = groupInvitationRepository.findById(groupInvitationId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_INVITATION_NOT_FOUND_ALERT, groupInvitationId)));
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (groupInvitation.getInviteeId().equals(currentUser.userId()) || utilsService.isUserAdminOrModerator(currentUser)) {
            switch (groupInvitation.getStatus()) {
                case REJECTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "You have already rejected the invitation to group " + groupInvitation.getGroupId());
                case INVITED -> {
                    groupInvitation.setStatus(InvitationStatus.REJECTED);
                    groupInvitationRepository.save(groupInvitation);
                }
                case ACCEPTED -> throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "You have already accepted the invitation to group " + groupInvitation.getGroupId());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not invited to group " + groupInvitation.getGroupId());
        }
        return groupService.getGroupResponseById(groupInvitation.getGroupId());
    }
}
