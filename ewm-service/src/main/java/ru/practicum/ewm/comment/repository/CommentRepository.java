package ru.practicum.ewm.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.module.Comment;
import ru.practicum.ewm.utility.Pagination;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(Long eventId, Pagination page);

    List<Comment> findByAuthorId(Long userId);

    Optional<Comment> findByAuthorIdAndId(Long userId, Long id);

    List<Comment> findByCommentContainingIgnoreCase(String text, Pagination page);
}
