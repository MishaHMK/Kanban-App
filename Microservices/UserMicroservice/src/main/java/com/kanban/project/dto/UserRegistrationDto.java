package com.kanban.project.dto;

import com.kanban.project.validation.FieldsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldsMatch(field = "password", fieldMatch = "repeatPassword", message = "Passwords do not match!")
public record UserRegistrationDto(
        @Email(message = "Please enter a valid email address")
        @NotBlank(message = "Email is required")
        @Size(max = 80, message = "Email must be at most 80 characters")
        String email,

        @NotBlank(message = "Nickname is required")
        @Size(min = 3, max = 25, message = "Nickname must be 3–25 characters")
        String nickname,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be 8–50 characters")
        String password,

        @NotBlank(message = "Please repeat your password")
        @Size(min = 8, max = 50, message = "Password must be 8–50 characters")
        String repeatPassword) {}
