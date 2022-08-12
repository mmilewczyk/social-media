package pl.mmilewczyk.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.userservice.model.dto.UserResponseWithId;
import pl.mmilewczyk.userservice.model.entity.Followship;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.repository.FollowshipRepository;
import pl.mmilewczyk.userservice.repository.UserRepository;

import java.util.List;

@Service
public record FollowshipService(FollowshipRepository followshipRepository,
                                UserRepository userRepository,
                                UtilsService utilsService) {

    public Page<UserResponseWithId> getFollowersOfUserByUserId(Long userId) {
        List<UserResponseWithId> followers = followshipRepository.findFollowshipsByFollowedUser_UserId(userId)
                .stream()
                .map(Followship::getFollowingUser)
                .map(User::mapToUserResponseWithId)
                .toList();
        return new PageImpl<>(followers);
    }

    public Page<UserResponseWithId> getFollowedUsersOfUserByUserId(Long userId) {
        List<UserResponseWithId> followers = followshipRepository.findFollowshipsByFollowingUser_UserId(userId)
                .stream()
                .map(Followship::getFollowedUser)
                .map(User::mapToUserResponseWithId)
                .toList();
        return new PageImpl<>(followers);
    }

    public List<UserResponseWithId> technicalGetFollowedUsersOfUserByUserId(Long userId) {
        return followshipRepository.findFollowshipsByFollowingUser_UserId(userId)
                .stream()
                .map(Followship::getFollowedUser)
                .map(User::mapToUserResponseWithId)
                .toList();
    }

    public UserResponseWithId followOtherUserById(Long userId) {
        User loggedInUser = userRepository.findById(utilsService.getLoggedInUser().userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not logged in"));
        User userToFollow = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id: %s does not exist", userId)));

        Followship followship = new Followship(loggedInUser, userToFollow);
        if (loggedInUser.getFollowedAmount() == null) {
            loggedInUser.setFollowedAmount(0L);
        }
        loggedInUser.setFollowedAmount(loggedInUser.getFollowedAmount() + 1);
        if (userToFollow.getFollowersAmount() == null) {
            userToFollow.setFollowersAmount(0L);
        }
        userToFollow.setFollowersAmount(userToFollow.getFollowersAmount() + 1);
        followshipRepository.save(followship);
        userRepository.save(loggedInUser);
        userRepository.save(userToFollow);
        return userToFollow.mapToUserResponseWithId();
    }

    public UserResponseWithId unfollowOtherUserById(Long userId) {
        User loggedInUser = userRepository.findById(utilsService.getLoggedInUser().userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not logged in"));
        User userToUnfollow = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id: %s does not exist", userId)));

        Followship followship = followshipRepository.findTop1FollowshipByFollowedUserAndFollowingUser(userToUnfollow, loggedInUser);
        if (loggedInUser.getFollowedAmount() == null) {
            loggedInUser.setFollowedAmount(0L);
        }
        loggedInUser.setFollowedAmount(loggedInUser.getFollowedAmount() - 1);
        if (userToUnfollow.getFollowersAmount() == null) {
            userToUnfollow.setFollowersAmount(0L);
        }
        userToUnfollow.setFollowersAmount(userToUnfollow.getFollowersAmount() - 1);
        if (followship == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "You don't follow this user");
        }
        followshipRepository.delete(followship);
        userRepository.save(loggedInUser);
        userRepository.save(userToUnfollow);
        return userToUnfollow.mapToUserResponseWithId();
    }
}
