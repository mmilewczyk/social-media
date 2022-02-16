package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.entity.Post;
import pl.mmilewczyk.postservice.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
public record PostService(PostRepository postRepository, UserClient userClient, NotificationClient notificationClient) {

    public PostResponse createNewPost(PostRequest postRequest) {
        ResponseEntity<UserResponseWithId> user = getCurrentUserFromUserService();

        Post post = Post.builder()
                .authorId(Objects.requireNonNull(user.getBody()).userId())
                .title(postRequest.title())
                .body(postRequest.body())
                .createdAt(LocalDateTime.now())
                .likes(0L)
                .comments(Collections.emptyList())
                .build();
        if (post.isComplete()) {
            postRepository.save(post);
            sendNotification(user.getBody(), postRequest);
            log.info("{} created new post {}", post.getAuthorId(), post.getPostId());
        } else {
            throw new NullPointerException("Fields cannot be empty!");
        }
        return new PostResponse(post.getTitle(), user.getBody().username(), post.getCreatedAt(), post.getBody(), post.getLikes(), post.getComments());
    }

    private ResponseEntity<UserResponseWithId> getCurrentUserFromUserService() {
        return userClient.getLoggedInUser();
    }

    private void sendNotification(UserResponseWithId userResponseWithId, PostRequest postRequest) {
        notificationClient.sendNotification(new NotificationRequest(
                        userResponseWithId.userId(),
                        userResponseWithId.email(),
                        String.format("Hi %s, you have just successfully created a new post '%s'",
                                userResponseWithId.username(), postRequest.title())));
    }
}
