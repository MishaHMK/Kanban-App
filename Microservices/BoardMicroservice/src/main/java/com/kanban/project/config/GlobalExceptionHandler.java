package com.kanban.project.config;

import com.kanban.project.dto.error.ErrorResponseDto;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.ForbiddenException;
import com.kanban.project.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            BoardServiceException eweRuntimeException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        eweRuntimeException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.error(eweRuntimeException.getMessage(), eweRuntimeException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}