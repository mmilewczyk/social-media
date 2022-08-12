package pl.mmilewczyk.notificationservice.controller;

import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.notificationservice.model.dto.NotificationRequest;
import pl.mmilewczyk.notificationservice.service.NotificationService;

@RestController
@RequestMapping("api/v1/notification")
public record NotificationController(NotificationService notificationService) {

    @PostMapping
    public void sendNotification(@RequestBody NotificationRequest notificationRequest) {
        notificationService.send(notificationRequest);
    }

    @PostMapping("/confirmAccount")
    public void sendAccountConfirmationEmail(@RequestParam String toEmail, @RequestParam String url) {
        notificationService.sendAccountConfirmationEmail(toEmail, url);
    }

    @PostMapping("/newComment")
    public void sendEmailAboutNewComment(@RequestBody NotificationRequest notificationRequest) {
        notificationService.sendEmailAboutNewComment(notificationRequest);
    }

    @PostMapping("/deletedComment")
    void sendEmailToTheCommentAuthorAboutDeletionOfComment(NotificationRequest notificationRequest){
        notificationService.sendEmailToTheCommentAuthorAboutDeletionOfComment(notificationRequest);
    }

    @PostMapping("/deletedPost")
    void sendEmailToThePostAuthorAboutDeletionOfPost(NotificationRequest notificationRequest) {
        notificationService.sendEmailToThePostAuthorAboutDeletionOfPost(notificationRequest);
    }

    @PostMapping("/editedComment")
    void sendEmailToTheCommentAuthorAboutEditionOfComment(NotificationRequest notificationRequest) {
        notificationService.sendEmailToTheCommentAuthorAboutEditionOfComment(notificationRequest);
    }

    @PostMapping("/deletedGroup")
    void sendEmailToTheGroupAuthorAboutDeletionOfGroup(NotificationRequest notificationRequest) {
        notificationService.sendEmailToTheGroupAuthorAboutDeletionOfGroup(notificationRequest);
    }

    @PostMapping("/newInvitation")
    void sendEmailToTheInviteeAboutInvitationToTheGroup(NotificationRequest notificationRequest) {
        notificationService.sendEmailToTheInviteeAboutInvitationToTheGroup(notificationRequest);
    }
}
