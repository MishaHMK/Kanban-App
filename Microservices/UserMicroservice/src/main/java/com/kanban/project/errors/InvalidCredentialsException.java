package com.kanban.project.errors;

public class InvalidCredentialsException extends UserServiceException {
    public InvalidCredentialsException(ExceptionMessage message) {
        super(message);
    }
}