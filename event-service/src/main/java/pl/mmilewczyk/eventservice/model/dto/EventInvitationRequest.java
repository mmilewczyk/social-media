package pl.mmilewczyk.eventservice.model.dto;

public record EventInvitationRequest(Long eventInvitationId, Long eventId, String eventName, Long inviterId, String inviterName, Long inviteeId, String invateeName) {
}
