package com.kanban.project.dto;

import com.kanban.project.validation.FieldsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldsMatch(field = "password", fieldMatch = "repeatPassword", message = "Passwords do not match!")
public record UserRegistrationDto(
        @Email @NotBlank @Size(max = 80) String email,
        @NotBlank @Size(min = 3, max = 25) String nickname,
        @NotBlank @Size(min = 8, max = 50) String password,
        @NotBlank @Size(min = 8, max = 50) String repeatPassword) {}
