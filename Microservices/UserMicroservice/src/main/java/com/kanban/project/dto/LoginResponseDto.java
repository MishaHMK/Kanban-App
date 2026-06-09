package com.kanban.project.dto;

public record LoginResponseDto(
        Long id,
        String email,
        String nickname,
        String token
) {}