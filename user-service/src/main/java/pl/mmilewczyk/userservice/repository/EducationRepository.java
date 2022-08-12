package pl.mmilewczyk.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.userservice.model.entity.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
