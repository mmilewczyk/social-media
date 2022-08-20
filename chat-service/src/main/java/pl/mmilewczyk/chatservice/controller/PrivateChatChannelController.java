package pl.mmilewczyk.chatservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.chatservice.model.dto.ChatMessageDTO;
import pl.mmilewczyk.chatservice.model.dto.EstablishedPrivateChatDTO;
import pl.mmilewczyk.chatservice.model.dto.PrivateChatDTO;
import pl.mmilewczyk.chatservice.service.ChatService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/chat/private-chat")
public class PrivateChatChannelController {

    private final ChatService chatService;

    @MessageMapping("/private.chat.{channelId}")
    @SendTo("/topic/private.chat.{channelId}")
    public ChatMessageDTO submitMessage(@DestinationVariable("channelId") String channelId, ChatMessageDTO message) {
        chatService.submitMessage(message);
        return message;
    }

    @PutMapping("/channel")
    public ResponseEntity<EstablishedPrivateChatDTO> establishChatChannel(@RequestBody PrivateChatDTO privateChatDTO) {
        return ResponseEntity.status(HttpStatus.FOUND).body(chatService.establishChatSession(privateChatDTO));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<ChatMessageDTO>> getExistingChatMessages(@PathVariable("channelId") Long channelId) {
        return ResponseEntity.ok(chatService.getExistingChatMessages(channelId));
    }
}
