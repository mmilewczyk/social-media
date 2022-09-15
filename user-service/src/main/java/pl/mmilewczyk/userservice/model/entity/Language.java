package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import pl.mmilewczyk.userservice.model.enums.LanguageLevel;
import pl.mmilewczyk.userservice.model.enums.LanguageName;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Language {

    @Id
    @SequenceGenerator(name = "language_id_sequence", sequenceName = "language_id_sequence")
    @GeneratedValue(strategy = SEQUENCE, generator = "language_id_sequence")
    private Long id;
    private LanguageName languageName;
    private LanguageLevel level;
}
