package pl.mmilewczyk.clients.user;

public record UserResponseWithId(Long userId, String username, String email, RangeDTO range) {
}
