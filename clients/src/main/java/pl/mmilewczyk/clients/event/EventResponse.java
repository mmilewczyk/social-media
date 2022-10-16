package pl.mmilewczyk.clients.event;

import pl.mmilewczyk.clients.post.PostResponse;
import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.util.List;

public record EventResponse(Long eventId,
                            String name,
                            String startAt,
                            String endAt,
                            String location,
                            UserResponseWithId organizer,
                            Boolean isPrivate,
                            String description,
                            List<String> hashtags,
                            List<UserResponseWithId> attendees,
                            List<UserResponseWithId> moderators,
                            List<PostResponse> posts) {
}
