package pl.mmilewczyk.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.notificationservice.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
