package pl.mmilewczyk.userservice.model.dto;

public record FollowshipResponse(Long id, Long followingUserId, Long followedUserId) {
}
