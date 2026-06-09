package com.kanban.project.NotificationCenter.error;

import lombok.Getter;

@Getter
public class NotificationCenterException extends RuntimeException {
    private final ExceptionMessage exceptionCode;

    public NotificationCenterException(ExceptionMessage exceptionCode) {
        super(exceptionCode.toString());
        this.exceptionCode = exceptionCode;
    }
}