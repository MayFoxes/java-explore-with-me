package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
public class UpdateCompilationDto {
    private Long id;
    private Set<Long> events;
    private Boolean pinned;
    @Size(max = 50)
    private String title;
}