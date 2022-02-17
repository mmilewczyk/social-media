package pl.mmilewczyk.userservice.model.dto;

public record UserResponseWithId(Long userId, String username, String email, RankDTO rank) {
}
