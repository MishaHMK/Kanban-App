package com.kanban.project.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardCreateRequestDto (
    @NotBlank
    @Size(min = 3, max = 30)
    String name) {
}
