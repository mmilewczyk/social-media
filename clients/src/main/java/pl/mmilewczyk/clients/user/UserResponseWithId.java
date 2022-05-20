package pl.mmilewczyk.clients.user;

import pl.mmilewczyk.clients.user.enums.*;

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
                                 Long followers,
                                 Long followed,
                                 Boolean notifyAboutComments,
                                 RoleName userRole) {
}