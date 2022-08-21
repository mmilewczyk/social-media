package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.event.EventResponse;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.groupservice.model.dto.GroupRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.model.dto.GroupResponseLite;
import pl.mmilewczyk.groupservice.model.entity.Group;
import pl.mmilewczyk.groupservice.repository.GroupRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UtilsService utilsService;
    private final NotificationClient notificationClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    private static final String GROUP_NOT_FOUND_ALERT = "The requested group with id %s was not found.";

    public GroupResponse createNewGroup(GroupRequest groupRequest) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group newGroup = new Group(
                groupRequest.groupName(),
                groupRequest.description(),
                Collections.emptyList(),
                currentUser.userId(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        if (newGroup.isComplete()) {
            groupRepository.saveAndFlush(newGroup);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "All fields should be filed");
        }
        return getGroupResponseById(newGroup.getGroupId());
    }

    private List<PostResponse> mapListOfPostResponseByPostsIds(Collection<Long> postIds) {
        List<PostResponse> finalPostList = new ArrayList<>();
        postIds.forEach(postId -> finalPostList.add(utilsService.getPostById(postId)));
        return finalPostList;
    }

    private List<UserResponseWithId> mapListOfUserResponseLiteByUsersIds(Collection<Long> userIds) {
        List<UserResponseWithId> finalUserResponseList = new ArrayList<>();
        userIds.forEach(userId -> finalUserResponseList.add(utilsService.getUserById(userId)));
        return finalUserResponseList;
    }

    public Page<GroupResponseLite> getGroupsByName(String groupName) {
        List<Group> groups = groupRepository.findGroupsByGroupNameIsLikeIgnoreCase(groupName);
        List<GroupResponseLite> mappedGroups = new ArrayList<>();
        groups.forEach(group -> mappedGroups.add(group.mapGroupToGroupResponseLite()));
        return new PageImpl<>(mappedGroups);
    }

    public Page<GroupResponseLite> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        List<GroupResponseLite> mappedGroups = new ArrayList<>();
        groups.forEach(group -> mappedGroups.add(group.mapGroupToGroupResponseLite()));
        return new PageImpl<>(mappedGroups);
    }

    public void deleteGroupById(Long groupId) {
        GroupResponse group = getGroupResponseById(groupId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (group.author().userId().equals(currentUser.userId()) || utilsService.isUserAdminOrModerator(currentUser)) {
            groupRepository.deleteById(groupId);
            if (utilsService.isUserAdminOrModerator(currentUser)) {
                sendEmailToTheGroupAuthorAboutDeletionOfGroup(groupId);
            }
        }
    }

    private void sendEmailToTheGroupAuthorAboutDeletionOfGroup(Long groupId) {
        GroupResponse groupResponse = getGroupResponseById(groupId);
        UserResponseWithId groupAuthor = groupResponse.author();
        NotificationRequest notificationRequest = new NotificationRequest(
                groupAuthor.userId(),
                groupAuthor.email(),
                String.format("Hi %s! Your group '%s' was deleted by a moderator.",
                        groupAuthor.username(), groupResponse.groupName()));
        notificationClient.sendEmailToTheGroupAuthorAboutDeletionOfGroup(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }

    public GroupResponse joinToGroup(Long groupId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group group = getGroupById(groupId);
        if (group.isUserAMemberOfGroup(currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are already member of the group %s", groupId));
        } else {
            group.getMembersIds().add(currentUser.userId());
            groupRepository.save(group);
        }
        return getGroupResponseById(groupId);
    }

    public GroupResponse leaveGroup(Long groupId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group group = getGroupById(groupId);
        if (group.getMembersIds().contains(currentUser.userId())) {
            group.getMembersIds().remove(currentUser.userId());
            groupRepository.save(group);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are not a member of the group %s", groupId));
        }
        return getGroupResponseById(groupId);
    }

    public GroupResponse makeSomeoneAModerator(Long groupId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Group group = getGroupById(groupId);
        if (group.getAuthorId().equals(currentUser.userId())) {
            if (group.getModeratorsIds().contains(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(
                        "User %s is aldread a moderator of the group %s", user.username(), groupId));
            } else {
                group.getModeratorsIds().add(userId);
                groupRepository.save(group);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner of the group");
        }
        return getGroupResponseById(groupId);
    }

    public GroupResponse deleteSomeoneAsAModerator(Long groupId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Group group = getGroupById(groupId);
        if (!group.getAuthorId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner of the group");
        }
        if (!group.getModeratorsIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(
                    "User %s is aldread not a moderator of the group %s", user.username(), groupId));
        }
        group.getModeratorsIds().remove(userId);
        groupRepository.save(group);

        return getGroupResponseById(groupId);
    }

    public GroupResponse removeSomeoneFromGroup(Long groupId, Long userToRemoveId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group group = getGroupById(groupId);
        if (group.getMembersIds().contains(userToRemoveId)) {
            if (group.getModeratorsIds().contains(userToRemoveId) || group.getAuthorId().equals(userToRemoveId)) {
                if (group.getAuthorId().equals(currentUser.userId())) {
                    group.getModeratorsIds().remove(userToRemoveId);
                    group.getMembersIds().remove(userToRemoveId);
                    groupRepository.save(group);
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not the group owner to remove a moderator");
                }
            } else if (group.getModeratorsIds().contains(currentUser.userId()) || group.getAuthorId().equals(currentUser.userId())) {
                group.getModeratorsIds().remove(userToRemoveId);
                group.getMembersIds().remove(userToRemoveId);
                groupRepository.save(group);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user you are trying to remove is not a member of the group");
        }
        return getGroupResponseById(groupId);
    }

    public GroupResponse editGroup(Long groupId, GroupRequest groupRequest) {
        Group group = getGroupById(groupId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (isGroupAdminOrModerator(currentUser, group)) {
            if (groupRequest.groupName() != null && !groupRequest.groupName().isEmpty()) {
                group.setGroupName(groupRequest.groupName());
            }
            if (groupRequest.description() != null) {
                group.setDescription(groupRequest.description());
            }
            groupRepository.save(group);
        }
        return getGroupResponseById(groupId);
    }

    public GroupResponse removeGroupMembersPost(Long groupId, Long postId) {
        Group group = getGroupById(groupId);
        if (group.getPostsIds().contains(postId)) {
            UserResponseWithId currentUser = utilsService.getCurrentUser();
            if (isGroupAdminOrModerator(currentUser, group)) {
                group.getPostsIds().remove(postId);
                groupRepository.save(group);
                return getGroupResponseById(groupId);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner or moderator of the group");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Post with id %s does not exist in group %s", postId, groupId));
    }

    private boolean isGroupAdminOrModerator(UserResponseWithId user, Group group) {
        return utilsService.isUserAdminOrModerator(user) ||
                group.getModeratorsIds().contains(user.userId()) ||
                group.getAuthorId().equals(user.userId());
    }

    public GroupResponse getGroupResponseById(Long groupId) {
        return mapGroupToGroupResponse(getGroupById(groupId));
    }

    private Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_NOT_FOUND_ALERT, groupId)));
    }

    private GroupResponse mapGroupToGroupResponse(Group group) {
        return new GroupResponse(
                group.getGroupId(),
                group.getGroupName(),
                group.getDescription(),
                mapListOfPostResponseByPostsIds(group.getPostsIds()),
                utilsService.getUserById(group.getAuthorId()),
                mapListOfUserResponseLiteByUsersIds(group.getModeratorsIds()),
                mapListOfUserResponseLiteByUsersIds(group.getMembersIds()),
                mapEventIdsToEventResponses(group.getEventsIds()));
    }

    List<EventResponse> mapEventIdsToEventResponses(List<Long> eventIds) {
        return eventIds.stream()
                .map(utilsService::getEventById)
                .collect(Collectors.toList());
    }
}
