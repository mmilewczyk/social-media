package pl.mmilewczyk.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.clients.event.EventClient;
import pl.mmilewczyk.clients.event.EventResponse;
import pl.mmilewczyk.clients.post.PostClient;
import pl.mmilewczyk.clients.post.PostRequest;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class UtilsService {

    private final UserClient userClient;
    private final PostClient postClient;
    private final EventClient eventClient;
    private final RestTemplate restTemplate;

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";
    private static final String POST_NOT_FOUND_ALERT = "The requested post was not found.";
    private static final String EVENT_NOT_FOUND_ALERT = "The requested event was not found.";

    UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", request.getHeader("Authorization"));
        ResponseEntity<UserResponseWithId> user = restTemplate.exchange(
                "http://USER-SERVICE/api/v1/users/profile",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponseWithId.class);
        if (user != null) return user.getBody();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    UserResponseWithId getUserById(Long id) {
        UserResponseWithId user = userClient.getUserById(id).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    PostResponse getPostById(Long postId) {
        PostResponse post = postClient.getPostById(postId).getBody();
        if (post != null) return post;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND_ALERT);
    }

    Boolean isUserAdminOrModerator(UserResponseWithId user) {
        RoleName userRole = user.userRole();
        return userRole.equals(RoleName.ADMIN) || userRole.equals(RoleName.MODERATOR);
    }

    EventResponse getEventById(Long id) {
        EventResponse event = eventClient.getEventById(id).getBody();
        if (event != null) return event;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_ALERT);
    }

    public PostResponse createNewPost(PostRequest postRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", request.getHeader("Authorization"));
        ResponseEntity<PostResponse> post = restTemplate.exchange(
                "http://POST-SERVICE/api/v1/posts",
                HttpMethod.POST,
                new HttpEntity<>(postRequest, headers),
                PostResponse.class);
        if (post != null) return post.getBody();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, POST_NOT_FOUND_ALERT);
    }
}
