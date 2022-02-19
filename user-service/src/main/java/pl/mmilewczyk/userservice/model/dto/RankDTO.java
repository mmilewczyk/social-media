package pl.mmilewczyk.userservice.model.dto;

import pl.mmilewczyk.userservice.model.enums.RankName;

public record RankDTO(RankName rangeName, String icon) {
}
