package com.kanban.project.errors;

public class UserNotFoundException  extends UserServiceException {
    public UserNotFoundException(ExceptionMessage message) {
        super(message);
    }
}