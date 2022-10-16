package pl.mmilewczyk.eventservice.model.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

public record EventRequest(@NotNull String name,
                           @NotNull @JsonFormat(shape = STRING, pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime startAt,
                           @NotNull @JsonFormat(shape = STRING, pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime endAt,
                           @NotNull String location,
                           @NotNull Boolean isPrivate,
                           @NotNull String description,
                           List<String> hashtags) {
}
