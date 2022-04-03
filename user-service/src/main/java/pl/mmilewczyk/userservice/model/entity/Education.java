package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import pl.mmilewczyk.userservice.model.enums.EducationLevel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Education {

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;
    private String nameOfUniversityOrSchool;
}
