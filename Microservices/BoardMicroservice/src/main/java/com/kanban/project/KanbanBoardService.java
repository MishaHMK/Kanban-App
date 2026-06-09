package com.kanban.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableFeignClients(basePackages = "com.kanban.project.client")
public class KanbanBoardService {
	public static void main(String[] args) {
		SpringApplication.run(KanbanBoardService.class, args);
	}
}
