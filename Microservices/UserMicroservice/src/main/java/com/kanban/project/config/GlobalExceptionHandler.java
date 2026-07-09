package com.kanban.project.config;

import com.kanban.project.errors.ErrorResponseDto;
import com.kanban.project.errors.InvalidCredentialsException;
import com.kanban.project.errors.UserAlreadyExistsException;
import com.kanban.project.errors.UserNotFoundException;
import com.kanban.project.errors.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleException(
            UserServiceException userServiceException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        userServiceException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.error(userServiceException.getMessage(), userServiceException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(
            BadRequestException badRequestException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        HttpStatus.BAD_REQUEST.value(),
                        badRequestException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.info(badRequestException.getMessage(), badRequestException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(
            UserNotFoundException userNotFoundException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND.value(),
                        userNotFoundException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.error(userNotFoundException.getMessage(), userNotFoundException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(
            UserAlreadyExistsException userServiceException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        HttpStatus.NOT_FOUND.value(),
                        userServiceException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.error(userServiceException.getMessage(), userServiceException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(
            InvalidCredentialsException invalidCredentialsException, ServletWebRequest servletWebRequest) {
        ErrorResponseDto errorResponseDTO =
                new ErrorResponseDto(
                        ZonedDateTime.now(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        HttpStatus.BAD_REQUEST.value(),
                        invalidCredentialsException.getMessage(),
                        servletWebRequest.getRequest().getRequestURI());

        log.info(invalidCredentialsException.getMessage(), invalidCredentialsException);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }
}
