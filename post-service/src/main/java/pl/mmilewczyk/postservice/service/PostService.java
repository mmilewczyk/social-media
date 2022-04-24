package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.comment.CommentClient;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.entity.Post;
import pl.mmilewczyk.postservice.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public record PostService(PostRepository postRepository,
                          UserClient userClient,
                          CommentClient commentClient,
                          NotificationClient notificationClient,
                          RabbitMQMessageProducer rabbitMQMessageProducer) {

    private static final String USER_NOT_FOUND_ALERT = "The requested user was not found.";
    private static final String POST_NOT_FOUND_ALERT = "The requested post %s was not found.";

    public PostResponse createNewPost(PostRequest postRequest) {
        UserResponseWithId user = getCurrentUser();
        Post post = createNewPostObject(postRequest, user);
        if (post.isComplete()) {
            postRepository.save(post);
            log.info("{} created new post {}", post.getAuthorId(), post.getPostId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The fields cannot be empty, complete them!");
        }
        return post.mapToPostResponse(user, null);
    }

    private Post createNewPostObject(PostRequest postRequest, UserResponseWithId user) {
        return Post.builder()
                .authorId(user.userId())
                .title(postRequest.title())
                .body(postRequest.body())
                .createdAt(LocalDateTime.now())
                .likes(0L)
                .commentsIds(Collections.emptyList())
                .build();
    }

    public void deletePostById(Long postId) {
        PostResponse post = getPostById(postId);
        UserResponseWithId currentUser = getCurrentUser();
        if (post.authorUsername().equals(currentUser.username()) || isUserAdminOrModerator(currentUser)) {
            postRepository.deleteById(postId);
            if (isUserAdminOrModerator(currentUser)) {
                sendEmailToThePostAuthorAboutDeletionOfPost(postId);
            }
        }
    }

    private Boolean isUserAdminOrModerator(UserResponseWithId user) {
        RoleName userRole = user.userRole();
        return userRole.equals(RoleName.ADMIN) || userRole.equals(RoleName.MODERATOR);
    }

    private void sendEmailToThePostAuthorAboutDeletionOfPost(Long postId) {
        PostResponse post = getPostById(postId);
        UserResponseWithId postAuthor = getUserByUsername(post.authorUsername());
        NotificationRequest notificationRequest = new NotificationRequest(
                postAuthor.userId(),
                postAuthor.email(),
                String.format("Hi %s! Your post '%s' was deleted by a moderator.",
                        postAuthor.username(), post.title()));
        notificationClient.sendEmailToThePostAuthorAboutDeletionOfPost(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }

    public Page<PostResponse> getSomeonePostsByUsername(String username, Pageable pageable) {
        UserResponseWithId foundUser = getUserByUsername(username);
        List<Post> posts = postRepository.findAllByAuthorId(foundUser.userId(), pageable);
        return mapListOfPostsToPageOfPostResponse(posts, foundUser);
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
                        String.format(POST_NOT_FOUND_ALERT, postId)));
        UserResponseWithId user = getUserById(post.getAuthorId());

        List<CommentResponse> commentResponses = new ArrayList<>();
        mapCommentIdsToCommentResponses(post, commentResponses);
        return post.mapToPostResponse(user, commentResponses);
    }

    private UserResponseWithId getUserByUsername(String username) {
        UserResponseWithId user = userClient.getUserByUsername(username).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    private UserResponseWithId getCurrentUser() {
        UserResponseWithId user = userClient.getLoggedInUser().getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    private UserResponseWithId getUserById(Long id) {
        UserResponseWithId user = userClient.getUserById(id).getBody();
        if (user != null) return user;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ALERT);
    }

    private void mapCommentIdsToCommentResponses(Post post, List<CommentResponse> commentResponses) {
        for (Long commentId : post.getCommentsIds()) {
            commentResponses.add(getCommentById(commentId));
        }
    }

    private CommentResponse getCommentById(Long id) {
        return commentClient.getCommentById(id).getBody();
    }

    public void giveLike(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(POST_NOT_FOUND_ALERT, id)));
        post.setLikes(post.getLikes() + 1);
    }
}
