package com.kanban.project.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private ZonedDateTime timestamp;

    private String status;

    private int statusCode;

    private String exceptionMessage;

    private String path;
}
