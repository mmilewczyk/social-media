package pl.mmilewczyk.eventservice.model.dto;

import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.time.LocalDateTime;
import java.util.List;

public record EventRequest(String name,
                           LocalDateTime startAt,
                           LocalDateTime endAt,
                           String location,
                           UserResponseWithId organizer,
                           Boolean isPrivate,
                           String description,
                           List<String> hashtags) {
}
