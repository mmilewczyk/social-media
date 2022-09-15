package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import pl.mmilewczyk.userservice.model.enums.EducationLevel;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Education {

    @Id
    @SequenceGenerator(name = "education_id_sequence", sequenceName = "education_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "education_id_sequence")
    private Long id;
    @Enumerated(STRING)
    private EducationLevel educationLevel;
    private String nameOfUniversityOrSchool;
}
