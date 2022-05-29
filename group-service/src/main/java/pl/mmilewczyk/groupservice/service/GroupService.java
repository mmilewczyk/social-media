package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;
import pl.mmilewczyk.groupservice.model.dto.GroupRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.model.dto.GroupResponseLite;
import pl.mmilewczyk.groupservice.model.entity.Group;
import pl.mmilewczyk.groupservice.repository.GroupRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UtilsService utilsService;

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
        return mapGroupToGroupResponse(newGroup);
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
                group.getEventsIds());
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

    public GroupResponse getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_NOT_FOUND_ALERT, groupId)));
        return mapGroupToGroupResponse(group);
    }

    public void deleteGroupById(Long groupId) {
        GroupResponse group = getGroupById(groupId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (group.author().userId().equals(currentUser.userId()) || isUserAdminOrModerator(currentUser)) {
            groupRepository.deleteById(groupId);
            if (isUserAdminOrModerator(currentUser)) {
                // todo: sendEmailToTheGroupAuthorAboutDeletionOfGroup(groupId);
            }
        }
    }

    private Boolean isUserAdminOrModerator(UserResponseWithId user) {
        RoleName userRole = user.userRole();
        return userRole.equals(RoleName.ADMIN) || userRole.equals(RoleName.MODERATOR);
    }

    public GroupResponse joinToGroup(Long groupId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group group = groupRepository.findById(groupId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_NOT_FOUND_ALERT, groupId)));
        if (group.getMembersIds().contains(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are already member of the group %s", groupId));
        } else {
            group.getMembersIds().add(currentUser.userId());
            groupRepository.save(group);
        }
        return mapGroupToGroupResponse(group);
    }

    public GroupResponse leaveGroup(Long groupId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        Group group = groupRepository.findById(groupId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_NOT_FOUND_ALERT, groupId)));
        if (group.getMembersIds().contains(currentUser.userId())) {
            group.getMembersIds().remove(currentUser.userId());
            groupRepository.save(group);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format("You are not a member of the group %s", groupId));
        }
        return mapGroupToGroupResponse(group);
    }

    public GroupResponse makeSomeoneAModerator(Long groupId, Long userId) {
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        UserResponseWithId user = utilsService.getUserById(userId);
        Group group = groupRepository.findById(groupId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GROUP_NOT_FOUND_ALERT, groupId)));
        if (group.getAuthorId().equals(currentUser.userId())) {
            if (group.getModeratorsIds().contains(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format(
                        "User %s is aldread a moderator of the group %s", user.username(), groupId));
            } else {
                group.getModeratorsIds().add(userId);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not owner of the group");
        }
        return mapGroupToGroupResponse(group);
    }

}
