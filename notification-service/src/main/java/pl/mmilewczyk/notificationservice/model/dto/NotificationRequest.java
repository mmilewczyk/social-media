package pl.mmilewczyk.notificationservice.model.dto;

public record NotificationRequest(Long toUserId, String toUserEmail, String message) {
}
