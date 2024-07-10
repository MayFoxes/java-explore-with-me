package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentDtoMapper;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.module.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.enums.EventState;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;
import ru.practicum.ewm.utility.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User tempUser = checkUser(userId);
        Event tempEvent = checkEvent(eventId);

        if (!tempEvent.getEventStatus().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("It is not possible to add a comment to the event with the status not PUBLISHED");
        }
        Comment newComment = commentRepository.save(CommentDtoMapper.toComment(dto, tempEvent, tempUser));
        return CommentDtoMapper.toDto(newComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto update) {
        User user = checkUser(userId);
        Comment comment = checkComm(commentId);
        checkAuthorComment(user, comment);

        if (LocalDateTime.now().isAfter(comment.getCreated().plusHours(1))) {
            throw new ConflictException("Messages can only be edited within an hour");
        }

        comment.setComment(update.getComment());
        comment.setEdited(true);
        return CommentDtoMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getUserComments(Long userId) {
        checkUser(userId);
        return CommentDtoMapper.toDtos(commentRepository.findByAuthorId(userId));
    }

    @Override
    public CommentDto getUserComment(Long userId, Long commentId) {
        checkUser(userId);
        Comment comment = commentRepository.findByAuthorIdAndId(userId, commentId)
                .orElseThrow(() -> new NotFoundException(String.format("No comment:%d found for user:%d", userId, commentId)));
        return CommentDtoMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsOfEvent(Long eventId, Integer from, Integer size) {
        checkEvent(eventId);
        return CommentDtoMapper.toDtos(commentRepository.findAllByEventId(eventId, new Pagination(from, size)));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = checkUser(userId);
        Comment comment = checkComm(commentId);
        checkAuthorComment(user, comment);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = checkComm(commentId);
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> findByText(String text, Integer from, Integer size) {
        return text.isBlank() ? new ArrayList<>() :
                CommentDtoMapper.toDtos(commentRepository.findByCommentContainingIgnoreCase(text, new Pagination(from, size)));
    }

    private Event checkEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event:%d not found", id)));
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User:%d not found", id)));
    }

    private Comment checkComm(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Comment:%d not found", id)));
    }

    private void checkAuthorComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new ValidationException(String.format("User:%d are not a author of comment:%d", user.getId(), comment.getId()));
        }
    }

}
