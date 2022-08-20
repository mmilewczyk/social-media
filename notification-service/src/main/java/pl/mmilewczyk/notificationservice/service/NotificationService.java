package pl.mmilewczyk.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.mmilewczyk.notificationservice.model.dto.NotificationRequest;
import pl.mmilewczyk.notificationservice.model.entity.Notification;
import pl.mmilewczyk.notificationservice.repository.NotificationRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public record NotificationService(NotificationRepository notificationRepository, JavaMailSender mailSender) {

    private static final String APPLICATION_EMAIL = "noreply.agiklo@gmail.com";

    public void send(NotificationRequest notificationRequest) {
        notificationRepository.save(
                Notification.builder()
                        .toUserId(notificationRequest.toUserId())
                        .toUserEmail(notificationRequest.toUserEmail())
                        .sender("social-media")
                        .message(notificationRequest.message())
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }

    public void sendAccountConfirmationEmail(String toEmail, String url) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(APPLICATION_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Confirm your account");
        message.setText(String.format("""
                Hello,
                Please verify the email address used with your SocialMedia account by clicking this link:
                %s
                All the best,
                SocialMedia team
                """, url)
        );
        log.info("Sending a confirmation email to {}", toEmail);
        mailSender.send(message);
        log.info("Confirmation email to {} has been sent", toEmail);
    }

    public void sendEmailAboutNewComment(NotificationRequest notificationRequest) {
        String subject = "Someone commented on your post! Check it out";
        log.info("Sending a email about new comment to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about new comment to {} has been sent", notificationRequest.toUserEmail());
    }

    public void sendEmailToTheCommentAuthorAboutDeletionOfComment(NotificationRequest notificationRequest) {
        String subject = "Your comment has been removed by our team.";
        log.info("Sending a email about deletion of comment to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about deletion of comment to {} has been sent", notificationRequest.toUserEmail());
    }

    public void sendEmailToThePostAuthorAboutDeletionOfPost(NotificationRequest notificationRequest) {
        String subject = "Your post has been removed by our team.";
        log.info("Sending a email about deletion of post to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about deletion of post to {} has been sent", notificationRequest.toUserEmail());
    }

    public void sendEmailToTheCommentAuthorAboutEditionOfComment(NotificationRequest notificationRequest) {
        String subject = "Your comment has been edited by our team.";
        log.info("Sending a email about edition of comment to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about edition of comment to {} has been sent", notificationRequest.toUserEmail());
    }

    public void sendEmailToTheGroupAuthorAboutDeletionOfGroup(NotificationRequest notificationRequest) {
        String subject = "Your group has been deleted by our team.";
        log.info("Sending a email about deleted group to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about deleted group to {} has been sent", notificationRequest.toUserEmail());
    }

    public void sendEmailToTheInviteeAboutInvitationToTheGroup(NotificationRequest notificationRequest) {
        String subject = "Your group has been deleted by our team.";
        log.info("Sending a email about new invitation to the group to {}", notificationRequest.toUserEmail());
        sendEmailBasicForm(notificationRequest, subject);
        log.info("Email about new invitation to the group to {} has been sent", notificationRequest.toUserEmail());
    }

    private void sendEmailBasicForm(NotificationRequest notificationRequest, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(APPLICATION_EMAIL);
        message.setTo(notificationRequest.toUserEmail());
        message.setSubject(subject);
        message.setText(notificationRequest.message());
        mailSender.send(message);
    }
}
