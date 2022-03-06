package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.comment.CommentClient;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
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
public record PostService(PostRepository postRepository,
                          UserClient userClient,
                          CommentClient commentClient,
                          RabbitMQMessageProducer rabbitMQMessageProducer) {

    public PostResponse createNewPost(PostRequest postRequest) {
        UserResponseWithId user = getCurrentUserFromUserService();

        assert user != null;
        Post post = Post.builder()
                .authorId(user.userId())
                .title(postRequest.title())
                .body(postRequest.body())
                .createdAt(LocalDateTime.now())
                .likes(0L)
                .commentsIds(Collections.emptyList())
                .build();
        if (post.isComplete()) {
            postRepository.save(post);
            sendNotification(user, postRequest);
            log.info("{} created new post {}", post.getAuthorId(), post.getPostId());
        } else {
            throw new NullPointerException("Fields cannot be empty!");
        }
        return post.mapToPostResponse(user, null);
    }

    private UserResponseWithId getCurrentUserFromUserService() {
        return userClient.getLoggedInUser().getBody();
    }

    private void sendNotification(UserResponseWithId userResponseWithId, PostRequest postRequest) {
        NotificationRequest notificationRequest = new NotificationRequest(
                userResponseWithId.userId(),
                userResponseWithId.email(),
                String.format("Hi %s, you have just successfully created a new post '%s'",
                        userResponseWithId.username(), postRequest.title()));

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key");
    }

    public Page<PostResponse> getSomeonePostsByUsername(String username, Pageable pageable) {
        UserResponseWithId foundUser = getUserByUsername(username);
        assert foundUser != null;
        List<Post> posts = postRepository.findAllByAuthorId(foundUser.userId(), pageable);
        return mapListOfPostsToPageOfPostResponse(posts, foundUser);
    }

    private UserResponseWithId getUserByUsername(String username) {
        return userClient.getUserByUsername(username).getBody();
    }

    public Page<PostResponse> mapListOfPostsToPageOfPostResponse(List<Post> posts, UserResponseWithId user) {
        List<CommentResponse> commentResponses = new ArrayList<>();
        List<PostResponse> mappedPosts = new ArrayList<>();
        for (Post post : posts) {
            mapCommentIdsToCommentResponses(post, commentResponses);
            mappedPosts.add(post.mapToPostResponse(user, commentResponses));
        }
        return new PageImpl<>(mappedPosts);
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Post with id: %s does not exist", postId)));
        UserResponseWithId user = getUserById(post.getAuthorId());

        List<CommentResponse> commentResponses = new ArrayList<>();
        mapCommentIdsToCommentResponses(post, commentResponses);
        return post.mapToPostResponse(user, commentResponses);
    }

    private UserResponseWithId getUserById(Long id) {
        return userClient.getUserById(id).getBody();
    }

    private List<CommentResponse> mapCommentIdsToCommentResponses(Post post, List<CommentResponse> commentResponses) {
        for (Long commentId : post.getCommentsIds()) {
            commentResponses.add(getCommentById(commentId));
        }
        return commentResponses;
    }

    private CommentResponse getCommentById(Long id) {
        return commentClient.getCommentById(id).getBody();
    }
}
