package pl.mmilewczyk.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.userservice.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
