package pl.mmilewczyk.commentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.mmilewczyk.amqp.RabbitMQMessageProducer;
import pl.mmilewczyk.clients.notification.NotificationClient;
import pl.mmilewczyk.clients.notification.NotificationRequest;
import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.clients.user.enums.RoleName;
import pl.mmilewczyk.commentservice.model.dto.CommentRequest;
import pl.mmilewczyk.commentservice.model.dto.CommentResponse;
import pl.mmilewczyk.commentservice.model.entity.Comment;
import pl.mmilewczyk.commentservice.repository.CommentRepository;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;
import static pl.mmilewczyk.clients.user.enums.RoleName.ADMIN;
import static pl.mmilewczyk.clients.user.enums.RoleName.MODERATOR;

@Slf4j
@Service
public record CommentService(CommentRepository commentRepository,
                             UtilsService utilsService,
                             NotificationClient notificationClient,
                             RabbitMQMessageProducer rabbitMQMessageProducer) {

    private static final String COMMENT_NOT_FOUND_ALERT = "The requested comment with id %s was not found.";
    private static final String INTERNAL_EXCHANGE = "internal.exchange";
    private static final String INTERNAL_NOTIFICATION_ROUTING_KEY = "internal.notification.routing-key";

    public CommentResponse createNewComment(CommentRequest commentRequest, Long postId) {
        UserResponseWithId user = utilsService.getCurrentUser();
        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(user.userId())
                .createdAt(now())
                .body(commentRequest.body())
                .likes(0L)
                .build();
        if (comment.isComplete()) {
            commentRepository.save(comment);
            sendMailToThePostAuthorAboutNewComment(postId, comment.getAuthorId());
            log.info("User {} added new comment {} to post {}", comment.getAuthorId(), comment.getCommentId(), postId);
        } else {
            throw new ResponseStatusException(NOT_ACCEPTABLE, "The fields cannot be empty, complete them!");
        }
        return comment.mapToCommentResponse(user);
    }

    private void sendMailToThePostAuthorAboutNewComment(Long postId, Long commentAuthorId) {
        PostResponse post = utilsService.getPostById(postId);
        UserResponseWithId postAuthor = utilsService.getUserByUsername(post.authorUsername());
        UserResponseWithId commentAuthor = utilsService.getUserById(commentAuthorId);
        if (postAuthor.notifyAboutComments()) {
            NotificationRequest notificationRequest = new NotificationRequest(
                    postAuthor.userId(),
                    postAuthor.email(),
                    format("Hi %s, %s commented on your post '%s'! See what was written.",
                            postAuthor.username(), commentAuthor.username(), post.title()));
            notificationClient.sendEmailAboutNewComment(notificationRequest);
            rabbitMQMessageProducer.publish(notificationRequest, INTERNAL_EXCHANGE, INTERNAL_NOTIFICATION_ROUTING_KEY);
        }
    }

    public void deleteCommentById(Long commentId) {
        CommentResponse comment = getCommentResponseById(commentId);
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (comment.authorUsername().equals(currentUser.username()) || isUserAdminOrModerator(currentUser)) {
            commentRepository.deleteById(commentId);
            if (isUserAdminOrModerator(currentUser)) {
                sendEmailToTheCommentAuthorAboutDeletionOfComment(commentId);
            }
        }
    }

    private void sendEmailToTheCommentAuthorAboutDeletionOfComment(Long commentId) {
        CommentResponse comment = getCommentResponseById(commentId);
        UserResponseWithId commentAuthor = utilsService.getUserByUsername(comment.authorUsername());
        NotificationRequest notificationRequest = new NotificationRequest(
                commentAuthor.userId(),
                commentAuthor.email(),
                format("Hi %s! Your comment '%s...' was deleted by a moderator.",
                        commentAuthor.username(), getCommentBodyToEmail(comment.body())));
        notificationClient.sendEmailToTheCommentAuthorAboutDeletionOfComment(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, INTERNAL_EXCHANGE, INTERNAL_NOTIFICATION_ROUTING_KEY);
    }

    private String getCommentBodyToEmail(String body) {
        if (body.length() > 100) {
            return body.substring(0, 25) + "...";
        } else {
            return body;
        }
    }

    private Boolean isUserAdminOrModerator(UserResponseWithId user) {
        RoleName userRole = user.userRole();
        return userRole.equals(ADMIN) || userRole.equals(MODERATOR);
    }

    public CommentResponse getCommentResponseById(Long commentId) {
        Comment comment = getCommentById(commentId);
        UserResponseWithId user = utilsService.getUserById(comment.getAuthorId());
        return comment.mapToCommentResponse(user);
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(COMMENT_NOT_FOUND_ALERT, commentId)));
    }

    public Page<CommentResponse> getAllCommentsOfThePost(Long id) {
        List<Comment> comments = commentRepository.findCommentsByPostId(id);
        List<CommentResponse> mappedComments = new LinkedList<>();
        comments.forEach(comment -> {
            UserResponseWithId user = utilsService.getUserById(comment.getAuthorId());
            mappedComments.add(comment.mapToCommentResponse(user));
        });
        return new PageImpl<>(mappedComments);
    }

    public CommentResponse editCommentById(Long commentId, CommentRequest commentRequest) {
        Comment comment = getCommentById(commentId);
        UserResponseWithId authorOfComment = utilsService.getUserById(comment.getAuthorId());
        UserResponseWithId currentUser = utilsService.getCurrentUser();
        if (authorOfComment.username().equals(currentUser.username()) || isUserAdminOrModerator(currentUser)) {
            comment.setBody(commentRequest.body());
            comment.setWasEdited(true);
            commentRepository.save(comment);
            if (isUserAdminOrModerator(currentUser)) {
                sendEmailToTheCommentAuthorAboutEditionOfComment(commentId);
            }
            return comment.mapToCommentResponse(authorOfComment);
        } else {
            throw new ResponseStatusException(UNAUTHORIZED);
        }
    }

    private void sendEmailToTheCommentAuthorAboutEditionOfComment(Long commentId) {
        CommentResponse comment = getCommentResponseById(commentId);
        UserResponseWithId commentAuthor = utilsService.getUserByUsername(comment.authorUsername());
        NotificationRequest notificationRequest = new NotificationRequest(
                commentAuthor.userId(),
                commentAuthor.email(),
                format("Hi %s! Your comment '%s...' was edited by a moderator.",
                        commentAuthor.username(), getCommentBodyToEmail(comment.body())));
        notificationClient.sendEmailToTheCommentAuthorAboutEditionOfComment(notificationRequest);
        rabbitMQMessageProducer.publish(notificationRequest, INTERNAL_EXCHANGE, INTERNAL_NOTIFICATION_ROUTING_KEY);
    }
}
