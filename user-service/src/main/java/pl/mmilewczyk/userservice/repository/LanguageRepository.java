package pl.mmilewczyk.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.userservice.model.entity.Language;

public interface LanguageRepository extends JpaRepository<Language, Long> {
}
