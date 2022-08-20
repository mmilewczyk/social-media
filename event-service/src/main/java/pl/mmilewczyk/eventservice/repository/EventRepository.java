package pl.mmilewczyk.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.eventservice.model.entity.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByNameLikeIgnoreCase(String name);
}
