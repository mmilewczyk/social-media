package pl.mmilewczyk.groupservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.groupservice.model.entity.Group;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findGroupsByGroupNameIsLikeIgnoreCase(String groupName);
}
