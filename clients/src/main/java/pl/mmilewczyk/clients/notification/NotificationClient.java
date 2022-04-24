package pl.mmilewczyk.clients.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("NOTIFICATION-SERVICE")
public interface NotificationClient {

    String BASE_URL = "api/v1/notification";

    @PostMapping(BASE_URL)
    void sendNotification(@RequestBody NotificationRequest notificationRequest);

    @PostMapping(BASE_URL + "/confirmAccount")
    void sendAccountConfirmationEmail(@RequestParam String toEmail, @RequestParam String url);

    @PostMapping(BASE_URL + "/newComment")
    void sendEmailAboutNewComment(@RequestBody NotificationRequest notificationRequest);

    @PostMapping(BASE_URL + "/deletedComment")
    void sendEmailToTheCommentAuthorAboutDeletionOfComment(NotificationRequest notificationRequest);

    @PostMapping(BASE_URL + "/deletedPost")
    void sendEmailToThePostAuthorAboutDeletionOfPost(NotificationRequest notificationRequest);
}
