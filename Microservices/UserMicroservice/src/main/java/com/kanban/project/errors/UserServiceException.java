package com.kanban.project.errors;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    private ExceptionMessage exceptionMessage;

    public UserServiceException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.toString());
        this.exceptionMessage = exceptionMessage;
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}