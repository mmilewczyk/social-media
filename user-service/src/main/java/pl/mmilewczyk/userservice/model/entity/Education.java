package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import pl.mmilewczyk.userservice.model.enums.EducationLevel;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Education {

    @Id
    @SequenceGenerator(name = "education_id_sequence", sequenceName = "education_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "education_id_sequence")
    private Long id;
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;
    private String nameOfUniversityOrSchool;
}
