package pl.mmilewczyk.groupservice.model.dto;

public record GroupInvitationRequest(Long groupInvitationId, String groupName, Long inviterId, String inviterName, Long inviteeId, String invateeName) {
}
