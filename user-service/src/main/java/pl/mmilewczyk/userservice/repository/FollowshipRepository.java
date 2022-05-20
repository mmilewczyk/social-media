package pl.mmilewczyk.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.userservice.model.entity.Followship;
import pl.mmilewczyk.userservice.model.entity.User;

import java.util.List;

public interface FollowshipRepository extends JpaRepository<Followship, Long> {

    Followship findFollowshipByFollowedUserAndFollowingUser(User followedUser, User followingUser);
    List<Followship> findFollowshipsByFollowingUser_UserId(Long followingUserId);
}
