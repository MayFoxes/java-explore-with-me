package ru.practicum.ewm.comment.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("POST request to add comment to event:{} from user:{}", eventId, userId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/users/{userId}/{commentId}")
    public CommentDto updateByUser(@PathVariable Long userId,
                                   @PathVariable Long commentId,
                                   @RequestBody @Valid NewCommentDto updateCommentDto) {
        log.info("PATCH request tu update comment:{} from user:{}", userId, commentId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getCommentsByUser(@PathVariable Long userId) {
        log.info("GET request to get comments from user{}", userId);
        return commentService.getUserComments(userId);
    }

    @DeleteMapping("/users/{userId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("DELETE request to delete comment:{} by user:{}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/users/{userId}/{commentId}")
    public CommentDto getComment(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("GET request to get comment:{} of user:{}", commentId, userId);
        return commentService.getUserComment(userId, commentId);
    }
}
