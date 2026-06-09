package com.kanban.project.error;

import com.kanban.project.error.model.ExceptionMessage;
import lombok.Getter;

@Getter
public class BoardServiceException extends RuntimeException {
    private final ExceptionMessage exceptionCode;

    public BoardServiceException(ExceptionMessage exceptionCode) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
    }
}