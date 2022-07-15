package pl.mmilewczyk.chatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.chatservice.model.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByAuthorIdAndRecipientIdOrderBySentAt(Long authorId, Long recipientId);
}
