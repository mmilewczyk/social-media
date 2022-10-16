package pl.mmilewczyk.eventservice.model.dto;

import pl.mmilewczyk.clients.user.UserResponseWithId;

import java.util.List;

public record PrivateEventResponse(Long eventId,
                                   String name,
                                   String startAt,
                                   String endAt,
                                   String location,
                                   UserResponseWithId organizer,
                                   Boolean isPrivate,
                                   String description,
                                   List<String> hashtags,
                                   Long attendeesAmount,
                                   Long postsAmount) {
}
