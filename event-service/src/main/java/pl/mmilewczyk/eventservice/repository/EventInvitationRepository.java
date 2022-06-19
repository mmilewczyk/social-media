package pl.mmilewczyk.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.eventservice.model.entity.EventInvitation;

public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
}
