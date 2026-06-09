package com.kanban.project.externals;

import com.kanban.project.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthServiceClient {
    @GetMapping("/users/search")
    List<UserDto> searchUsers(@RequestParam("query") String query, @RequestParam("excludeId") Long excludeId);
}