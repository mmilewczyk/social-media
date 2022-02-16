package pl.mmilewczyk.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.postservice.model.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
