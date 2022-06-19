package pl.mmilewczyk.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.eventservice.model.entity.EventRequestToJoin;
import pl.mmilewczyk.eventservice.model.enums.Status;

import java.util.List;

public interface EventRequestToJoinRepository extends JpaRepository<EventRequestToJoin, Long> {

    List<EventRequestToJoin> getEventRequestToJoinsByStatusAndEventId(Status status, Long eventId);
    List<EventRequestToJoin> getEventRequestToJoinsByPersonJoiningId(Long personJoiningId);
}
