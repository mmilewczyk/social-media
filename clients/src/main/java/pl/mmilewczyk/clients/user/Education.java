package pl.mmilewczyk.clients.user;

import pl.mmilewczyk.clients.user.enums.EducationLevel;

public record Education(Long id,
                        EducationLevel educationLevel,
                        String nameOfUniversityOrSchool){
}
