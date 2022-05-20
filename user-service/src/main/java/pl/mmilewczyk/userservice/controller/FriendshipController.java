package pl.mmilewczyk.userservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.service.FollowshipService;

@RestController
@RequestMapping("api/v1/users/profile")
public record FriendshipController(FollowshipService followshipService) {

    @PutMapping("/{userId}/follow")
    public ResponseEntity<UserResponseWithId> followOtherUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(followshipService.followOtherUserById(userId));
    }

    @PutMapping("/{userId}/unfollow")
    public ResponseEntity<UserResponseWithId> unfollowOtherUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(followshipService.unfollowOtherUserById(userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<UserResponseWithId>> getFollowersOfUserByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(followshipService.getFollowersOfUserByUserId(userId));
    }

    @GetMapping("/{userId}/followed")
    public ResponseEntity<Page<UserResponseWithId>> getFollowedUsersOfUserByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(followshipService.getFollowedUsersOfUserByUserId(userId));

    }
}
