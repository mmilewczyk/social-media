package pl.mmilewczyk.userservice.model.dto;

import pl.mmilewczyk.userservice.model.entity.Education;
import pl.mmilewczyk.userservice.model.entity.Language;
import pl.mmilewczyk.userservice.model.enums.Gender;
import pl.mmilewczyk.userservice.model.enums.LookingFor;
import pl.mmilewczyk.userservice.model.enums.RelationshipStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record UserEditRequest(String username,
                              String firstName,
                              @NotNull LocalDate birth,
                              @NotNull Gender gender,
                              @NotNull String currentCity,
                              @NotNull String homeTown,
                              @NotNull List<Language> languagesISpeak,
                              List<Language> languagesImLearning,
                              @NotNull List<LookingFor> lookingFor,
                              Education education,
                              String occupationOrJob,
                              RelationshipStatus relationshipStatus,
                              String aboutMe,
                              Boolean notifyAboutComments) {
}
