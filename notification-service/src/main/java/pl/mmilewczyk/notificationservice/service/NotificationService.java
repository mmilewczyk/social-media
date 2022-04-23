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
}
