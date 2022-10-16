package pl.mmilewczyk.chatservice.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.mmilewczyk.chatservice.model.dto.ChatMessageDTO;
import pl.mmilewczyk.chatservice.model.dto.PrivateChatDTO;
import pl.mmilewczyk.chatservice.model.entity.ChatMessage;
import pl.mmilewczyk.chatservice.model.entity.PrivateChatChannel;
import pl.mmilewczyk.chatservice.repository.ChatMessageRepository;
import pl.mmilewczyk.chatservice.repository.PrivateChatChannelRepository;
import pl.mmilewczyk.clients.user.UserClient;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Service
public record ChatService(UserClient userClient,
                          SimpMessagingTemplate simpMessagingTemplate,
                          PrivateChatChannelRepository privateChatChannelRepository,
                          ChatMessageRepository chatMessageRepository) {

    private PrivateChatChannel getExistingChannel(PrivateChatDTO privateChatDTO) {
        List<PrivateChatChannel> channel = privateChatChannelRepository
                .findExistingChannel(privateChatDTO.firstUserId(), privateChatDTO.secondUserId());
        return (channel != null && !channel.isEmpty()) ? channel.get(0) : null;
    }

//    private Long newChatSession(PrivateChatDTO privateChatDTO) {
//        PrivateChatChannel channel = new PrivateChatChannel(
//                requireNonNull(userClient.().getBody()).userId(),
//                requireNonNull(userClient.getUserById(privateChatDTO.secondUserId()).getBody()).userId());
//        privateChatChannelRepository.save(channel);
//        return channel.getId();
//    }

//    public EstablishedPrivateChatDTO establishChatSession(PrivateChatDTO privateChatDTO) {
//        if (Objects.equals(privateChatDTO.firstUserId(), privateChatDTO.secondUserId())) {
//            throw new ResponseStatusException(NOT_ACCEPTABLE, "Users are the same");
//        }
//
//        PrivateChatChannel privateChat = getExistingChannel(privateChatDTO);
//
//        Long privateChatId = (privateChat != null) ? privateChat.getId() : newChatSession(privateChatDTO);
//        assert privateChat != null;
//        return new EstablishedPrivateChatDTO(
//                privateChatId,
//                requireNonNull(userClient.getUserById(privateChat.getFirstUserId()).getBody()).username(),
//                requireNonNull(userClient.getUserById(privateChat.getSecondUserId()).getBody()).username());
//    }

    public void submitMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = new ChatMessage(
                chatMessageDTO.authorId(),
                chatMessageDTO.recipientId(),
                chatMessageDTO.content(),
                now());

        chatMessageRepository.save(chatMessage);

        UserResponseWithId fromUser = userClient.getUserById(chatMessage.getAuthorId()).getBody();
        UserResponseWithId recipientUser = userClient.getUserById(chatMessage.getRecipientId()).getBody();

//        userService.notifyUser(recipientUser,
//                new NotificationDTO(
//                        "ChatMessageNotification",
//                        fromUser.getFullName() + " has sent you a message",
//                        chatMessage.getAuthorUser().getId()
//                )
//        );
    }

    public List<ChatMessageDTO> getExistingChatMessages(Long chatId) {
        PrivateChatChannel channel = privateChatChannelRepository.getById(chatId);
        List<ChatMessage> chatMessages = chatMessageRepository.
                findAllByAuthorIdAndRecipientIdOrderBySentAt(
                        channel.getFirstUserId(), channel.getSecondUserId());

        List<ChatMessageDTO> chatMessageDTOS = new ArrayList<>();
        chatMessages.forEach(chatMessage -> chatMessageDTOS.add(new ChatMessageDTO(
                        chatMessage.getAuthorId(),
                        chatMessage.getRecipientId(),
                        chatMessage.getContent())));
        return chatMessageDTOS;
    }
}
