package ru.practicum.ewm.compilation.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.events.dto.EventDtoMapper;

import java.util.stream.Collectors;

@UtilityClass
public class CompilationDtoMapper {
    public CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream()
                        .map(EventDtoMapper::toEventShortDto)
                        .collect(Collectors.toSet()))
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .build();
    }

    public Compilation toCompilation(NewCompilationDto compilationDto) {
        return Compilation.builder()
                .pinned(compilationDto.isPinned())
                .title(compilationDto.getTitle())
                .build();
    }
}