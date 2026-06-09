package com.kanban.project.error;

import com.kanban.project.error.model.ExceptionMessage;

public class NotFoundException extends BoardServiceException {
    public NotFoundException(ExceptionMessage message) {
        super(message);
    }
}
