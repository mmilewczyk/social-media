package pl.mmilewczyk.userservice.model.dto.auth;

import lombok.Builder;

@Builder
public record SuccessfulAuthDto(Long id, String username, String token) {
}
