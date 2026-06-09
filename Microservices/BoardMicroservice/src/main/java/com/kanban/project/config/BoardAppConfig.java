package com.kanban.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "board")
@Getter
@Setter
public class BoardAppConfig {
    private Integer columnsLimit;
    private Integer tasksLimit;
}
