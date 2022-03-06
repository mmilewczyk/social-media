package pl.mmilewczyk.commentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.comment.CommentResponse;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.post.PostClient;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.commentservice.model.dto.CommentRequest;
import pl.mmilewczyk.commentservice.model.entity.Comment;
import pl.mmilewczyk.commentservice.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public record CommentService(CommentRepository commentRepository,
                             UserClient userClient,
                             PostClient postClient,
                             RabbitMQMessageProducer rabbitMQMessageProducer) {

    public CommentResponse createNewComment(CommentRequest commentRequest, Long postId) {
        UserResponseWithId user = getCurrentUserFromUserService();

        assert user != null;
        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(user.userId())
                .createdAt(LocalDateTime.now())
                .body(commentRequest.body())
                .likes(0L)
                .build();

        if (comment.isComplete()) {
            commentRepository.save(comment);
            sendNotificationToThePostAuthor(postId, comment.getAuthorId());
            log.info("{} created new comment {}", comment.getAuthorId(), comment.getCommentId());
        } else {
            throw new NullPointerException("Fields cannot be empty!");
        }
        return comment.mapToCommentResponse(user);
    }

    private UserResponseWithId getCurrentUserFromUserService() {
        return userClient.getLoggedInUser().getBody();
    }

    private void sendNotificationToThePostAuthor(Long postId, Long commentAuthorId) {

        PostResponse post = getPostById(postId);
        UserResponseWithId postAuthor = getUserByUsername(post.authorUsername());
        UserResponseWithId commentAuthor = getUserById(commentAuthorId);

        NotificationRequest notificationRequest = new NotificationRequest(
                postAuthor.userId(),
                postAuthor.email(),
                String.format("Hi %s, user %s added a comment to your post '%s'",
                        postAuthor.username(), commentAuthor.username(), post.title()));

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key");
    }

    private UserResponseWithId getUserByUsername(String username) {
        return userClient.getUserByUsername(username).getBody();
    }

    private PostResponse getPostById(Long postId) {
        return postClient.getPostById(postId).getBody();
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Comment with id %s does not exist", commentId)));
        UserResponseWithId user = getUserById(comment.getAuthorId());
        return comment.mapToCommentResponse(user);
    }

    private UserResponseWithId getUserById(Long userId) {
        return userClient.getUserById(userId).getBody();
    }

    public Page<CommentResponse> getAllCommentsOfThePost(Long id) {
        PostResponse post = getPostById(id);
        List<CommentResponse> comments = post.comments();
        return new PageImpl<>(comments);
    }
}
