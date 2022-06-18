package pl.mmilewczyk.eventservice.model.dto;

import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(String name,
                            LocalDateTime startAt,
                            LocalDateTime endAt,
                            String location,
                            UserResponseWithId organizer,
                            Boolean isPrivate,
                            String description,
                            List<String> hashtags,
                            List<UserResponseWithId> attendees,
                            List<PostResponse> posts) {
}