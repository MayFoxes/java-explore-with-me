package ru.practicum.ewm.users.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class NewUserRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String name;
}