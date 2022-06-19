package pl.mmilewczyk.eventservice.model.dto;

public record EventInvitationRequest(Long eventInvitationId, String eventName, Long inviterId, String inviterName, Long inviteeId, String invateeName) {
}
