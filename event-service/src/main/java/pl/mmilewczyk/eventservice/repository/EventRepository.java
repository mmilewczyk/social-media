package pl.mmilewczyk.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.eventservice.model.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
