package pl.mmilewczyk.notificationservice.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.mmilewczyk.notificationservice.model.dto.NotificationRequest;
import pl.mmilewczyk.notificationservice.service.NotificationService;

@Slf4j
@Component
public record NotificationConsumer(NotificationService notificationService) {

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void consumer(NotificationRequest notificationRequest) {
        log.info("Consumed {} from queue", notificationRequest);
        notificationService.send(notificationRequest);
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void sendAccountConfirmationEmail(String toEmail, String link) {
        log.info("Queue consumed confirmation link: '{}' and it's sending it to the {}", link, toEmail);
        notificationService.sendAccountConfirmationEmail(toEmail, link);
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void sendEmailAboutNewComment(NotificationRequest notificationRequest) {
        log.info("Queue consumed new comment and it's notifying author {}", notificationRequest.toUserEmail());
        notificationService.sendEmailAboutNewComment(notificationRequest);
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void sendEmailToTheCommentAuthorAboutDeletionOfComment(NotificationRequest notificationRequest) {
        log.info("Queue consumed new deletion of comment and it's notifying author {}", notificationRequest.toUserEmail());
        notificationService.sendEmailToTheCommentAuthorAboutDeletionOfComment(notificationRequest);
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void sendEmailToThePostAuthorAboutDeletionOfPost(NotificationRequest notificationRequest) {
        log.info("Queue consumed new deletion of post and it's notifying author {}", notificationRequest.toUserEmail());
        notificationService.sendEmailToThePostAuthorAboutDeletionOfPost(notificationRequest);
    }
}
