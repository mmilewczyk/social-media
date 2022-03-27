package pl.mmilewczyk.userservice.model.dto;

import lombok.Builder;

@Builder
public record SuccessfulAuthDto(Long id, String username, String token) {
}
