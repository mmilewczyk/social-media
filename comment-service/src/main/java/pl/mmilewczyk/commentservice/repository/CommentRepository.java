package pl.mmilewczyk.commentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mmilewczyk.commentservice.model.entity.Comment;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByPostId(Long postId);
}
