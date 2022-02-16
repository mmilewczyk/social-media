package pl.mmilewczyk.userservice.model.dto;

import pl.mmilewczyk.userservice.model.enums.RangeName;

public record RangeDTO(RangeName rangeName, String icon) {
}
