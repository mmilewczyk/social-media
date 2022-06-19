package pl.mmilewczyk.eventservice.model.dto;

import pl.mmilewczyk.clients.user.UserResponseWithId;
import pl.mmilewczyk.eventservice.model.enums.Status;

public record EventRequestToJoinResponse(Long eventRequestToJoinId, Long eventId, String eventName, UserResponseWithId personJoining, Status status) {
}
