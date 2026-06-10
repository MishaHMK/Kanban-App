package com.kanban.project.errors;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    private final ExceptionMessage exceptionMessage;

    public UserServiceException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.toString());
        this.exceptionMessage = exceptionMessage;
    }
}