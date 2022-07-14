package pl.mmilewczyk.postservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.dto.PostResponseLite;
import pl.mmilewczyk.postservice.model.entity.Post;
import pl.mmilewczyk.postservice.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public record PostService(PostRepository postRepository,
                          UtilsService utilsService,
                          NotificationClient notificationClient,
                          RabbitMQMessageProducer rabbitMQMessageProducer) {

    private static final String POST_NOT_FOUND_ALERT = "The requested post %s was not found.";

    public PostResponse createNewPost(PostRequest postRequest) {
        UserResponseWithId user = utilsService.getCurrentUser();
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
        UserResponseWithId currentUser = utilsService.getCurrentUser();
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
        UserResponseWithId postAuthor = utilsService.getUserByUsername(post.authorUsername());
        NotificationRequest notificationRequest = new NotificationRequest(
                postAuthor.userId(),
                postAuthor.email(),
                String.format("Hi %s! Your post '%s' was deleted by a moderator.",
                        postAuthor.username(), post.title()));
        notificationClient.sendEmailToThePostAuthorAboutDeletionOfPost(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, "internal.exchange", "internal.notification.routing-key");
    }

    public Page<PostResponseLite> getSomeonePostsByUsername(String username) {
        UserResponseWithId foundUser = utilsService.getUserByUsername(username);
        List<Post> posts = postRepository.findAllByAuthorId(foundUser.userId());
        List<PostResponseLite> mappedPosts = mapListOfPostToListOfPostResponseLite(posts);
        return new PageImpl<>(mappedPosts);
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(POST_NOT_FOUND_ALERT, postId)));
        UserResponseWithId user = utilsService.getUserById(post.getAuthorId());

        List<CommentResponse> commentResponses = new ArrayList<>();
        mapCommentIdsToCommentResponses(post, commentResponses);
        return post.mapToPostResponse(user, commentResponses);
    }

    private void mapCommentIdsToCommentResponses(Post post, List<CommentResponse> commentResponses) {
        for (Long commentId : post.getCommentsIds()) {
            commentResponses.add(utilsService.getCommentById(commentId));
        }
    }

    public void giveLike(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(POST_NOT_FOUND_ALERT, id)));
        post.setLikes(post.getLikes() + 1);
    }

    public Page<PostResponseLite> getPostByTitle(String title) {
        List<Post> posts = postRepository.findAllByTitleLikeIgnoreCase(title);
        List<PostResponseLite> mappedPosts = mapListOfPostToListOfPostResponseLite(posts);
        return new PageImpl<>(mappedPosts);
    }

    private List<PostResponseLite> mapListOfPostToListOfPostResponseLite(List<Post> posts) {
        if (!posts.isEmpty()) {
            List<PostResponseLite> mappedPosts = new ArrayList<>();
            for (Post post : posts) {
                UserResponseWithId user = utilsService.getUserById(post.getAuthorId());
                mappedPosts.add(post.mapToPostResponseLite(user));
            }
            return mappedPosts;
        } else {
            return Collections.emptyList();
        }
    }

    public PostResponse updatePostById(PostRequest postRequest, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(POST_NOT_FOUND_ALERT, postId)));
        UserResponseWithId authorOfPost = utilsService.getUserById(post.getAuthorId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (authorOfPost.username().equals(currentUser.username()) || isUserAdminOrModerator(currentUser)) {
            post.setTitle(postRequest.title());
            post.setBody(postRequest.body());
            postRepository.save(post);
            if (isUserAdminOrModerator(currentUser)) {
                // TODO: sendEmailToThePostAuthorAboutEditionOfPost(postId);
            }
            List<CommentResponse> commentResponses = new ArrayList<>();
            mapCommentIdsToCommentResponses(post, commentResponses);
            return post.mapToPostResponse(authorOfPost, commentResponses);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    public Page<PostResponseLite> getAllLatestPosts() {
        List<Post> posts = postRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostResponseLite> mappedPosts = mapListOfPostToListOfPostResponseLite(posts);
        return new PageImpl<>(mappedPosts);
    }

    public Page<PostResponseLite> getAllLatestPostsOfFollowedPeople() {
        List<UserResponseWithId> followedUsers = utilsService.getFollowedUsersByUserId(
                utilsService.getCurrentUser().userId());

        List<Post> foundedPosts = new ArrayList<>();
        followedUsers.forEach(followedUser -> foundedPosts.addAll(
                postRepository.findAllByAuthorId(followedUser.userId())));
        List<PostResponseLite> mappedPosts = mapListOfPostToListOfPostResponseLite(foundedPosts);
        return new PageImpl<>(mappedPosts);
    }
}
