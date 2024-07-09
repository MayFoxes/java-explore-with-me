package ru.practicum.ewm.comment.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comment.module.Comment;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.users.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentDtoMapper {
    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .edited(comment.getEdited())
                .build();
    }

    public Comment toComment(NewCommentDto dto, Event event, User user) {
        return Comment.builder()
                .comment(dto.getComment())
                .event(event)
                .author(user)
                .created(LocalDateTime.now())
                .edited(false)
                .build();
    }

    public List<CommentDto> toDtos(List<Comment> comments) {
        return comments.stream()
                .map(CommentDtoMapper::toDto)
                .collect(Collectors.toList());
    }

}
