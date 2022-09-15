package pl.mmilewczyk.commentservice.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostClient;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

@Service
public record UtilsService(UserClient userClient, PostClient postClient, RestTemplate restTemplate) {

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";
    private static final String POST_NOT_FOUND_ALERT = "The requested post was not found.";

    UserResponseWithId getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, request.getHeader(AUTHORIZATION));
        ResponseEntity<UserResponseWithId> user = restTemplate.exchange(
                "http://USER-SERVICE/api/v1/users/profile",
                GET,
                new HttpEntity<>(headers),
                UserResponseWithId.class);
        if (user.getBody() == null) {
            throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
        }
        return user.getBody();
    }

    UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
        }
        return user;
    }

    UserResponseWithId getUserById(Long userId) {
        UserResponseWithId user = userClient.getUserById(userId).getBody();
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
        }
        return user;
    }

    PostResponse getPostById(Long postId) {
        PostResponse post = postClient.getPostById(postId).getBody();
        if (post == null) {
            throw new ResponseStatusException(NOT_FOUND, POST_NOT_FOUND_ALERT);
        }
        return post;
    }
}
