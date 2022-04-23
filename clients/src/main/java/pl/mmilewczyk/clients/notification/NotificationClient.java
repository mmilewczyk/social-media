package pl.mmilewczyk.clients.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("api/v1/notification")
    void sendNotification(NotificationRequest notificationRequest);

    @PostMapping("api/v1/notification/confirmAccount")
    void sendAccountConfirmationEmail(@RequestParam String toEmail, @RequestParam String url);
}
