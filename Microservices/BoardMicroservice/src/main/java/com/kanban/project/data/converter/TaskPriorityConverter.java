package com.kanban.project.data.converter;

import com.kanban.project.data.Priority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TaskPriorityConverter implements AttributeConverter<Priority, String> {
    @Override
    public String convertToDatabaseColumn(Priority priority) {
        return priority == null ? null : priority.getDbName();
    }

    @Override
    public Priority convertToEntityAttribute(String string) {
        return string == null ? null : Priority.get(string);
    }
}