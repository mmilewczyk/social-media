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
    public void send(String toEmail, String link) {
        log.info("Queue consumed confirmation link: '{}' and it's sending it to the {}", link, toEmail);
        notificationService.sendAccountConfirmationEmail(toEmail, link);
    }
}
