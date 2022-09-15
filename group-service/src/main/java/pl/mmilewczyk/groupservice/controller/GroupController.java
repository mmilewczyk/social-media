package pl.mmilewczyk.groupservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.clients.post.PostRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupRequest;
import pl.mmilewczyk.groupservice.model.dto.GroupResponse;
import pl.mmilewczyk.groupservice.model.dto.GroupResponseLite;
import pl.mmilewczyk.groupservice.service.GroupService;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createNewGroup(@RequestBody GroupRequest groupRequest) {
        return status(CREATED).body(groupService.createNewGroup(groupRequest));
    }

    @GetMapping
    public ResponseEntity<Page<GroupResponseLite>> getAllGroups() {
        return status(OK).body(groupService.getAllGroups());
    }

    @GetMapping("/search/groupName")
    public ResponseEntity<Page<GroupResponseLite>> getGroupsByName(@RequestParam("groupName") String groupName) {
        return status(FOUND).body(groupService.getGroupsByName(groupName));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable("groupId") Long groupId) {
        return status(FOUND).body(groupService.getGroupResponseById(groupId));
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteGroupById(@PathVariable("groupId") Long groupId) {
        groupService.deleteGroupById(groupId);
    }

    @PutMapping("/join/{groupId}")
    public ResponseEntity<GroupResponse> joinToGroup(@PathVariable("groupId") Long groupId) {
        return status(OK).body(groupService.joinToGroup(groupId));
    }

    @PutMapping("/leave/{groupId}")
    public ResponseEntity<GroupResponse> leaveGroup(@PathVariable("groupId") Long groupId) {
        return status(OK).body(groupService.leaveGroup(groupId));
    }

    @PutMapping("/edit/add/moderator")
    public ResponseEntity<GroupResponse> makeSomeoneAModerator(@RequestParam Long groupId,
                                                               @RequestParam Long userId) {
        return status(OK).body(groupService.makeSomeoneAModerator(groupId, userId));
    }

    @PutMapping("/edit/remove/moderator")
    public ResponseEntity<GroupResponse> removeSomeoneAsAModerator(@RequestParam Long groupId, @RequestParam Long userId) {
        return status(OK).body(groupService.deleteSomeoneAsAModerator(groupId, userId));
    }

    @PutMapping("/remove-user")
    public ResponseEntity<GroupResponse> removeSomeoneFromGroup(@RequestParam Long groupId,
                                                                @RequestParam Long userToRemoveId) {
        return status(OK).body(groupService.removeSomeoneFromGroup(groupId, userToRemoveId));
    }

    @PutMapping("/edit/{groupId}")
    public ResponseEntity<GroupResponse> editGroup(@RequestBody GroupRequest groupRequest, @PathVariable Long groupId) {
        return status(CREATED).body(groupService.editGroup(groupId, groupRequest));
    }

    @PutMapping("/edit/posts/remove")
    public ResponseEntity<GroupResponse> removeGroupMembersPost(@RequestParam Long groupId, @RequestParam Long postId) {
        return status(ACCEPTED).body(groupService.removeGroupMembersPost(groupId, postId));
    }

    @PutMapping("{groupId}/create/post")
    public ResponseEntity<GroupResponse> addPostToGroup(@PathVariable("groupId") Long groupId,
                                                        @RequestBody PostRequest postRequest) {
        return status(CREATED).body(groupService.addPostToGroup(groupId, postRequest));
    }

    @PutMapping("{groupId}/connect/event/{eventId}")
    public ResponseEntity<GroupResponse> connectEventToGroup(@PathVariable("groupId") Long groupId,
                                                             @PathVariable("eventId") Long eventId) {
        return status(CREATED).body(groupService.connectEventToGroup(groupId, eventId));
    }
}
