package pl.mmilewczyk.eventservice.model.dto;


import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record EventRequest(@NotNull String name,
                           @NotNull LocalDateTime startAt,
                           @NotNull LocalDateTime endAt,
                           @NotNull String location,
                           @NotNull Boolean isPrivate,
                           @NotNull String description,
                           List<String> hashtags) {
}
