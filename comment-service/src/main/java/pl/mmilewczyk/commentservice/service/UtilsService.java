package pl.mmilewczyk.commentservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostClient;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;

@Service
public record UtilsService(UserClient userClient, PostClient postClient) {

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";
    private static final String POST_NOT_FOUND_ALERT = "The requested post was not found.";

    UserResponseWithId getCurrentUser() {
        UserResponseWithId user = userClient.getLoggedInUser().getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getUserById(Long userId) {
        UserResponseWithId user = userClient.getUserById(userId).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    PostResponse getPostById(Long postId) {
        PostResponse post = postClient.getPostById(postId).getBody();
        if (post != null) return post;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND_ALERT);
    }
}
