package com.kanban.project.error;

import com.kanban.project.error.model.ExceptionMessage;

public class ForbiddenException extends BoardServiceException {
    public ForbiddenException(ExceptionMessage message) {
        super(message);
    }
}
