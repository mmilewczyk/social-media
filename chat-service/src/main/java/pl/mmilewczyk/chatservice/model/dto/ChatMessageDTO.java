package pl.mmilewczyk.chatservice.model.dto;

public record ChatMessageDTO(Long authorId, Long recipientId, String content) {
}
