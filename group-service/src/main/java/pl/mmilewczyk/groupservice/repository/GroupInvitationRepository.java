package pl.mmilewczyk.groupservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.groupservice.model.entity.GroupInvitation;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
}
