package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.comment.CommentClient;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public record UtilsService(UserClient userClient, CommentClient commentClient) {

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";

    UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getCurrentUser() {
        UserResponseWithId user = userClient.getLoggedInUser().getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getUserById(Long id) {
        UserResponseWithId user = userClient.getUserById(id).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    CommentResponse getCommentById(Long id) {
        return commentClient.getCommentById(id).getBody();
    }

    List<UserResponseWithId> getFollowedUsersByUserId(Long userId) {
        return Objects.requireNonNull(userClient.getFollowedUsersOfUserByUserId(userId)
                        .getBody())
                .getContent();
    }
}
