package pl.mmilewczyk.clients.user;

import pl.mmilewczyk.clients.user.enums.LanguageLevel;
import pl.mmilewczyk.clients.user.enums.LanguageName;

public record Language(Long id,
                       LanguageName languageName,
                       LanguageLevel level) {
}
