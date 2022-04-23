package pl.mmilewczyk.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pl.mmilewczyk.userservice.model.entity.User;
import pl.mmilewczyk.userservice.model.enums.Gender;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User a " +
            "SET a.isEnabled = TRUE WHERE a.username = ?1")
    int enableUser(String username);


    List<User> findAllByGenderOrCurrentCity(Gender gender, String currentCity);
}
