package com.kanban.project.mapper;

import com.kanban.project.dto.UserRegistrationDto;
import com.kanban.project.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegistrationDto userRegistrationDto);
}
