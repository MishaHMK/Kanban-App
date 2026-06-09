package com.kanban.project.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum Priority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String dbName;

    Priority(String dbName) {
        this.dbName = dbName;
    }

    @JsonCreator
    public static Priority get(String key) {
        String normalizedValue = key.trim().toLowerCase();
        return switch (normalizedValue) {
            case "low" -> LOW;
            case "medium" -> MEDIUM;
            case "high" -> HIGH;
            default -> throw new IllegalArgumentException(key + " not supported.");
        };
    }
}