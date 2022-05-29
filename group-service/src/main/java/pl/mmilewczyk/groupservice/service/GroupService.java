package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
}
