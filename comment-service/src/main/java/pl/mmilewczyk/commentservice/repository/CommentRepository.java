package pl.mmilewczyk.commentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.commentservice.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
