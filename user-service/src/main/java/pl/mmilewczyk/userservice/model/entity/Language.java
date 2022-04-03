package pl.mmilewczyk.userservice.model.entity;

import lombok.*;
import pl.mmilewczyk.userservice.model.enums.LanguageLevel;
import pl.mmilewczyk.userservice.model.enums.LanguageName;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Language {

    @Id
    @SequenceGenerator(name = "language_id_sequence", sequenceName = "language_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_id_sequence")
    private Long id;
    private LanguageName languageName;
    private LanguageLevel level;
}
