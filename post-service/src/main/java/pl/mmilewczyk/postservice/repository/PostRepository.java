package pl.mmilewczyk.postservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.postservice.model.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByAuthorId(Long authorId);
    List<Post> findAllByTitleLikeIgnoreCase(String title);
}
