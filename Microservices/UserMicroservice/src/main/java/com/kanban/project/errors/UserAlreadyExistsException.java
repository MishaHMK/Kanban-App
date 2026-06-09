package com.kanban.project.errors;

public class UserAlreadyExistsException extends UserServiceException {
    public UserAlreadyExistsException(ExceptionMessage message)  {
        super(message);
    }
}