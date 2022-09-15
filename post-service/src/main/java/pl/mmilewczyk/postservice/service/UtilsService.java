package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.comment.CommentClient;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

@Slf4j
@Service
public record UtilsService(UserClient userClient, CommentClient commentClient, RestTemplate restTemplate) {

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";

    UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, request.getHeader(AUTHORIZATION));
        ResponseEntity<UserResponseWithId> user = restTemplate.exchange(
                "http://USER-SERVICE/api/v1/users/profile", GET, new HttpEntity<>(headers), UserResponseWithId.class);
        if (user.getBody() != null) return user.getBody();
        else throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getUserById(Long id) {
        UserResponseWithId user = userClient.getUserById(id).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    CommentResponse getCommentById(Long id) {
        return commentClient.getCommentById(id).getBody();
    }

    List<CommentResponse> getAllCommentsOfThePost(Long id) {
        return commentClient.technicalGetAllCommentsOfThePost(id);
    }

    void deleteCommentById(Long commentId) {
        commentClient.deleteCommentById(commentId);
    }

    List<UserResponseWithId> getFollowedUsersByUserId(Long userId) {
        List<UserResponseWithId> followedUsersOfUserByUserId =
                userClient.technicalGetFollowedUsersOfUserByUserId(userId);
        if (followedUsersOfUserByUserId == null) {
            throw new NullPointerException("followedUsersOfUserByUserId response is null");
        }
        return followedUsersOfUserByUserId;
    }
}
