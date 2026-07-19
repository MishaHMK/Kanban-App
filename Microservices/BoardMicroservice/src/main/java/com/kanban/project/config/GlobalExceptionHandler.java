package com.kanban.project.config;

import com.kanban.project.dto.error.ErrorResponseDto;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.ForbiddenException;
import com.kanban.project.error.NotFoundException;
import com.kanban.project.error.model.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.ZonedDateTime;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(
            ForbiddenException forbiddenException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        HttpStatus.FORBIDDEN.value(),
                        forbiddenException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.info(forbiddenException.getMessage(), forbiddenException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(
            NotFoundException notFoundException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND.value(),
                        notFoundException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.info(notFoundException.getMessage(), notFoundException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BoardServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleException(
            BoardServiceException boardServiceException, ServletWebRequest servletWebRequest) {
        HttpStatus status = resolveStatus(boardServiceException.getExceptionCode());

        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        status.getReasonPhrase(),
                        status.value(),
                        boardServiceException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        if (status.is5xxServerError()) {
            log.error(boardServiceException.getMessage(), boardServiceException);
        } else {
            log.info(boardServiceException.getMessage(), boardServiceException);
        }

        return new ResponseEntity<>(errorResponseDTO, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException validationException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        HttpStatus.BAD_REQUEST.value(),
                        ExceptionMessage.VALIDATION_FAILED.toString(),
                        servletWebRequest.getRequest().getRequestURI());

        log.info("Validation failed: {}", validationException.getMessage());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    private HttpStatus resolveStatus(ExceptionMessage code) {
        return switch (code) {
            case BOARD_COLUMN_LIMIT_REACHED, COLUMN_TASK_LIMIT_REACHED -> HttpStatus.CONFLICT;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}