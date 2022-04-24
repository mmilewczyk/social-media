package pl.mmilewczyk.userservice.model.dto;

import pl.mmilewczyk.userservice.model.entity.Education;
import pl.mmilewczyk.userservice.model.entity.Language;
import pl.mmilewczyk.userservice.model.enums.LookingFor;
import pl.mmilewczyk.userservice.model.enums.RelationshipStatus;
import pl.mmilewczyk.userservice.model.enums.RoleName;

import java.util.List;

public record UserResponseWithId(Long userId,
                                 String username,
                                 String email,
                                 String rank,
                                 String firstName,
                                 String age,
                                 String currentCity,
                                 String homeTown,
                                 List<Language> learn,
                                 List<Language> speaks,
                                 List<LookingFor> lookingFor,
                                 Education education,
                                 RelationshipStatus relationshipStatus,
                                 String aboutMe,
                                 List<Long> friendsIds,
                                 Boolean notifyAboutComments,
                                 RoleName userRole) {
}
