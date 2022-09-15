package pl.mmilewczyk.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.post.PostClient;
import pl.mmilewczyk.clients.post.PostRequest;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;
import static pl.mmilewczyk.clients.user.enums.RoleName.ADMIN;
import static pl.mmilewczyk.clients.user.enums.RoleName.MODERATOR;

@Service
@RequiredArgsConstructor
public class UtilsService {

    private final UserClient userClient;
    private final PostClient postClient;
    private final RestTemplate restTemplate;

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";
    private static final String POST_NOT_FOUND_ALERT = "The requested post was not found.";

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
                "http://USER-SERVICE/api/v1/users/profile",
                GET,
                new HttpEntity<>(headers),
                UserResponseWithId.class);
        if (user.getBody() != null) return user.getBody();
        else throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    public UserResponseWithId getUserById(Long id) {
        UserResponseWithId user = userClient.getUserById(id).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    PostResponse getPostById(Long postId) {
        PostResponse post = postClient.getPostById(postId).getBody();
        if (post != null) return post;
        else throw new ResponseStatusException(NOT_FOUND, POST_NOT_FOUND_ALERT);
    }

    Boolean isUserAdminOrModerator(UserResponseWithId user) {
        RoleName userRole = user.userRole();
        return userRole.equals(ADMIN) || userRole.equals(MODERATOR);
    }

    public PostResponse createNewPost(PostRequest postRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, request.getHeader(AUTHORIZATION));
        ResponseEntity<PostResponse> post = restTemplate.exchange(
                "http://POST-SERVICE/api/v1/posts",
                POST,
                new HttpEntity<>(postRequest, headers),
                PostResponse.class);
        if (post.getBody() != null) return post.getBody();
        else throw new ResponseStatusException(NOT_FOUND, POST_NOT_FOUND_ALERT);
    }
}