package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.postservice.mapper.PostMapper;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.entity.Post;
import pl.mmilewczyk.postservice.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public record PostService(PostRepository postRepository, UserClient userClient, NotificationClient notificationClient, PostMapper postMapper) {

    public PostResponse createNewPost(PostRequest postRequest) {
        UserResponseWithId user = getCurrentUserFromUserService().getBody();

        assert user != null;
        Post post = Post.builder()
                .authorId(user.userId())
                .title(postRequest.title())
                .body(postRequest.body())
                .createdAt(LocalDateTime.now())
                .likes(0L)
                .comments(Collections.emptyList())
                .build();
        if (post.isComplete()) {
            postRepository.save(post);
            sendNotification(user, postRequest);
            log.info("{} created new post {}", post.getAuthorId(), post.getPostId());
        } else {
            throw new NullPointerException("Fields cannot be empty!");
        }
        return postMapper.mapPostToPostResponse(post, user);
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

    public Page<PostResponse> getSomeonePostsByUsername(String username, Pageable pageable) {
        UserResponseWithId findedUser = userClient.getUserByUsername(username).getBody();
        assert findedUser != null;
        List<Post> posts = postRepository.findAllByAuthorId(findedUser.userId(), pageable);
        return mapListOfPostsToPageOfPostResponse(posts, findedUser);
    }

    public Page<PostResponse> mapListOfPostsToPageOfPostResponse(List<Post> posts, UserResponseWithId user) {
        List<PostResponse> mappedPosts = new ArrayList<>();
        for (Post post : posts) {
            mappedPosts.add(postMapper.mapPostToPostResponse(post, user));
        }
        return new PageImpl<>(mappedPosts);
    }
}
